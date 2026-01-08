package com.telemetry.engine.auth.security.jwt.key.store.impl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemetry.engine.auth.security.jwt.key.enums.JwtKeyStatus;
import com.telemetry.engine.auth.security.jwt.key.model.JwtKeys;
import com.telemetry.engine.auth.security.jwt.key.store.JwtKeyStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DescribeSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ResourceExistsException;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsJwtKeyStore implements JwtKeyStore {

  private static final String SECRET_NAME = "jwt/keys";

  private final SecretsManagerClient client;
  private final ObjectMapper mapper;

  private JwtKeys cache;

  private void reloadCache() {
    log.info("[AwsJwtKeyStore.reloadCache] Fetching keys from AWS secrets");
    try {
      GetSecretValueResponse response =
          client.getSecretValue(GetSecretValueRequest.builder().secretId(SECRET_NAME).build());

      if (response.secretString() != null) {
        cache = mapper.readValue(response.secretString(), JwtKeys.class);
      } else {
        cache = new JwtKeys();
        cache.setKeys(new ArrayList<>());
      }

    } catch (ResourceNotFoundException e) {
      log.warn("JWT secret not found in AWS, initializing empty");
      cache = new JwtKeys();
      cache.setKeys(new ArrayList<>());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load JWT keys from AWS", e);
    }
  }

  private void saveCache() {
    log.info("[AwsJwtKeyStore.saveCache] Writing to AWS secrets");
    try {
      String payload = mapper.writeValueAsString(cache);
      try {
        client.describeSecret(DescribeSecretRequest.builder().secretId(SECRET_NAME).build());
        client.putSecretValue(
            PutSecretValueRequest.builder().secretId(SECRET_NAME).secretString(payload).build());
      } catch (ResourceNotFoundException e) {
        client.createSecret(
            CreateSecretRequest.builder().name(SECRET_NAME).secretString(payload).build());
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to save JWT keys to AWS", e);
    }
  }

  @Override
  public Optional<JwtKeys> findKeys() {
    log.info("[AwsJwtKeyStore.findKeys] Fetching keys");
    reloadCache();
    return Optional.ofNullable(cache);
  }

  @Override
  public void saveKeys(JwtKeys keys) {
    log.info("[AwsJwtKeyStore.saveKeys] Saving keys");
    cache = keys;
    saveCache();
  }


  @Override
  public boolean rotateKeysAtomically(String expectedCurrentKid, JwtKeys newKeys) {
    try {
      // Fetching current state
      GetSecretValueResponse currentSecret =
          client.getSecretValue(GetSecretValueRequest.builder().secretId(SECRET_NAME).build());

      JwtKeys existing = mapper.readValue(currentSecret.secretString(), JwtKeys.class);

      // Optimistic Checking: Does the DB still have the key we think it has?
      boolean matches = existing.getKeys().stream().anyMatch(
          k -> k.getStatus() == JwtKeyStatus.CURRENT && k.getKid().equals(expectedCurrentKid));

      if (!matches) {
        log.info("Rotation skipped: Kid mismatch. Another instance likely rotated already.");
        return false;
      }

      /*
       * Putting Secret value with a NEW idempotency token. This creates a NEW version and AWS moves
       * the AWSCURRENT label to it automatically.
       */
      PutSecretValueRequest putRequest = PutSecretValueRequest.builder().secretId(SECRET_NAME)
          .secretString(mapper.writeValueAsString(newKeys))
          .clientRequestToken(UUID.randomUUID().toString()) // New unique token for this attempt
          .build();

      client.putSecretValue(putRequest);

      log.info("JWT keys rotated successfully in AWS Secrets Manager.");
      return true;

    } catch (ResourceExistsException e) {
      // This happens if two instances generate the same UUID or a retry happens
      log.warn("Conflict detected in Secrets Manager. Treating as already rotated.");
      return false;
    } catch (Exception e) {
      log.error("Failed to rotate keys in AWS", e);
      return false;
    }
  }


}
