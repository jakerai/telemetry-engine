package com.telemetry.engine.auth.security.jwt.key.service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.telemetry.engine.auth.config.JwtProperties;
import com.telemetry.engine.auth.security.jwt.key.enums.JwtKeyStatus;
import com.telemetry.engine.auth.security.jwt.key.model.JwtKeys;
import com.telemetry.engine.auth.security.jwt.key.model.Key;
import com.telemetry.engine.auth.security.jwt.key.store.JwtKeyStore;
import com.telemetry.engine.common.constansts.Algorithms;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtKeyManager {

  private final JwtKeyStore jwtKeyStore;
  private final KeyEncryptionService encryption;
  private final RsaKeyGenerator generator;
  private final JwtProperties props;

  private volatile Key currentKey;
  private volatile Key previousKey;

  private volatile PrivateKey cachedPrivateKey;
  private final Map<String, RSAPublicKey> publicKeyCache = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    log.info("[JwtKeyManager.init] Loading");
    try {
      refreshInternalState();
    } catch (Exception e) {
      log.error("Failed to initialize JWT keys", e);
      throw new IllegalStateException("JWT key initialization failed", e);
    }
  }

  private synchronized void refreshInternalState() {
    log.info(
        "[JwtKeyManager.refreshInternalState] Refreshing internal key state and rebuilding cache");
    Optional<JwtKeys> keysOpt = jwtKeyStore.findKeys();

    if (keysOpt.isPresent() && !keysOpt.get().getKeys().isEmpty()) {
      List<Key> keys = keysOpt.get().getKeys();
      this.currentKey = keys.stream().filter(k -> k.getStatus() == JwtKeyStatus.CURRENT).findFirst()
          .orElseGet(this::createNewKeyAndSave);

      this.previousKey = keys.stream().filter(k -> k.getStatus() == JwtKeyStatus.PREVIOUS)
          .findFirst().orElse(null);
    } else {
      this.currentKey = createNewKey();
      this.previousKey = null;
      saveKeys();
    }

    // Updating caches whenever state is refreshed
    rebuildCaches();
  }

  private void rebuildCaches() {
    log.info("[JwtKeyManager.rebuildCaches] Caching private and public keys");
    // Caching the Private Key for signing
    this.cachedPrivateKey = decryptPrivateKey(this.currentKey);

    // Caching Public Keys for validation
    publicKeyCache.clear();
    publicKeyCache.put(currentKey.getKid(), buildPublicKey(currentKey.getPublicKey()));
    if (previousKey != null) {
      publicKeyCache.put(previousKey.getKid(), buildPublicKey(previousKey.getPublicKey()));
    }
  }

  public PrivateKey getCurrentPrivateKey() {
    log.info("[JwtKeyManager.getCurrentPrivateKey] Fetching private key");
    return this.cachedPrivateKey;
  }

  public RSAPublicKey getCurrentPublicKey() {
    log.info("[JwtKeyManager.getCurrentPublicKey] Fetching public key for the current kid");
    return getPublicKeyByKid(currentKey.getKid());
  }

  public RSAPublicKey getPublicKeyByKid(String kid) {
    log.info("[JwtKeyManager.getPublicKeyByKid] Fetching public key for the provided kid");
    RSAPublicKey key = publicKeyCache.get(kid);

    /*
     * If kid is missing from cache, another instance might have rotated (multi-instance). Sync once
     * to be sure before failing.
     */
    if (key == null) {
      refreshInternalState();
      key = publicKeyCache.get(kid);
    }

    if (key == null) {
      throw new IllegalStateException("Unknown kid: " + kid);
    }
    return key;
  }

  private PrivateKey decryptPrivateKey(Key localKey) {
    log.info("[JwtKeyManager.decryptPrivateKey] Decrypting private key");
    try {
      byte[] decrypted =
          encryption.decrypt(Base64.getDecoder().decode(localKey.getEncryptedPrivateKey()));
      return KeyFactory.getInstance(Algorithms.RSA)
          .generatePrivate(new PKCS8EncodedKeySpec(decrypted));
    } catch (Exception e) {
      log.info("Error while decrypting private key");
      throw new RuntimeException("Failed to decrypt private key", e);
    }
  }

  private RSAPublicKey buildPublicKey(String base64) {
    log.info("[JwtKeyManager.buildPublicKey] Building public key for base64={}", base64);
    try {
      byte[] decoded = Base64.getDecoder().decode(base64);
      return (RSAPublicKey) KeyFactory.getInstance(Algorithms.RSA)
          .generatePublic(new X509EncodedKeySpec(decoded));
    } catch (Exception e) {
      log.info("Error while building public key from base64");
      throw new RuntimeException("Failed to build RSAPublicKey", e);
    }
  }

  @Scheduled(fixedDelayString = "${app.security.jwt.rotation.check-interval-ms}")
  public void scheduledRotationCheck() {
    try {
      rotateIfNeeded();
    } catch (Exception e) {
      log.error("Error during scheduled rotation check", e);
    }
  }

  private synchronized void rotateIfNeeded() {
    long rotateBeforeSeconds = props.getRotation().getRotateBeforeSeconds();
    log.info("[JwtKeyManager.rotateIfNeeded] Checking if rotation is required: rotateBeforeSeconds={}", rotateBeforeSeconds);
    Key localCurrent = this.currentKey;
    Key localPrevious = this.previousKey;

    boolean shouldRotate =
        localCurrent.getExpiresAt().minusSeconds(rotateBeforeSeconds).isBefore(Instant.now())
            || (localPrevious != null && localPrevious.getExpiresAt().isBefore(Instant.now()));

    if (!shouldRotate) {
      log.info("Rotating keys: No");
      return;
    }

    log.info("Rotating keys: Yes");

    // Using builder to avoid mutating the object directly before persistence
    Key newPrevious = localCurrent.toBuilder().status(JwtKeyStatus.PREVIOUS).build();
    Key newCurrent = createNewKey();

    JwtKeys jwtKeys = JwtKeys.builder().keys(List.of(newPrevious, newCurrent)).build();
    boolean success = jwtKeyStore.rotateKeysAtomically(newPrevious.getKid(), jwtKeys);

    if (success) {
      log.info("Already success");
      this.previousKey = newPrevious;
      this.currentKey = newCurrent;
      rebuildCaches();
    } else {
      log.info("Already rotated somewhere else (multi-instance). Syncing local state.");
      refreshInternalState();
    }
  }

  private Key createNewKey() {
    log.info("[JwtKeyManager.createNewKey] Creating new key");
    var kp = generator.generate();
    byte[] encryptedBytes = encryption.encrypt(kp.getPrivate().getEncoded());

    return Key.builder().kid(UUID.randomUUID().toString())
        .publicKey(Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()))
        .encryptedPrivateKey(Base64.getEncoder().encodeToString(encryptedBytes))
        .status(JwtKeyStatus.CURRENT)
        .expiresAt(Instant.now().plusSeconds(props.getRotation().getValiditySeconds())).build();
  }

  private Key createNewKeyAndSave() {
    log.info("[JwtKeyManager.createNewKeyAndSave] Creating and saving new key");
    Key key = createNewKey();
    this.currentKey = key;
    saveKeys();
    return key;
  }

  private void saveKeys() {
    log.info("[JwtKeyManager.saveKeys] Saving keys");
    List<Key> list = previousKey != null ? List.of(previousKey, currentKey) : List.of(currentKey);
    jwtKeyStore.saveKeys(JwtKeys.builder().keys(list).build());
  }

  public Key getCurrentKey() {
    log.info("[JwtKeyManager.getCurrentKey] Fetching current key");
    return this.currentKey;
  }


}
