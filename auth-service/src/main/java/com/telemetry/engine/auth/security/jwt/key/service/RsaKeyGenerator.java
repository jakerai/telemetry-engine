package com.telemetry.engine.auth.security.jwt.key.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.springframework.stereotype.Component;
import com.telemetry.engine.auth.config.AppSecurityProperties;
import com.telemetry.engine.common.constansts.Algorithms;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RsaKeyGenerator {

  private final AppSecurityProperties props;

  /**
   * Generates a new RSA public/private key pair for JWT signing.
   *
   * @return the generated KeyPair
   */
  public KeyPair generate() {
    log.info("[RsaKeyGenerator.generate] Generating a new RSA public/private key pair");
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance(Algorithms.RSA);
      gen.initialize(props.getJwt().getRsa().getKeySize());
      return gen.generateKeyPair();
    } catch (Exception e) {
      log.error("Error while generating a new RSA public/private key pair");
      throw new RuntimeException("Failed to generate RSA key", e);
    }
  }

}
