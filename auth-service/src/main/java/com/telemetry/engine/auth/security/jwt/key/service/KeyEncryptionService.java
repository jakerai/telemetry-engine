package com.telemetry.engine.auth.security.jwt.key.service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import com.telemetry.engine.auth.config.AppSecurityProperties;
import com.telemetry.engine.common.constansts.Algorithms;
import com.telemetry.engine.common.utils.SecretUtils;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class KeyEncryptionService {

  private final String secret;

  public KeyEncryptionService(AppSecurityProperties jwtProperties) {
    this.secret = jwtProperties.getJwt().getEncryption().getMasterSecret();
    log.info("[KeyEncryptionService] loading encryption key secret={}",
        SecretUtils.maskSensitiveData(secret));

    if (!(secret.length() == 16 || secret.length() == 24 || secret.length() == 32)) {
      throw new IllegalArgumentException("AES key must be 16, 24, or 32 bytes long");
    }
  }

  public byte[] encrypt(byte[] data) {
    try {
      Cipher cipher = Cipher.getInstance(Algorithms.AES);
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret.getBytes(), Algorithms.AES));
      return cipher.doFinal(data);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] decrypt(byte[] data) {
    try {
      Cipher cipher = Cipher.getInstance(Algorithms.AES);
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secret.getBytes(), Algorithms.AES));
      return cipher.doFinal(data);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
