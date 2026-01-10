package com.telemetry.engine.auth.security.apikey;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiKeyGenerator {

  /**
   * Generates secret key
   * @return
   */
  public static final String generate() {
    log.info("Generating API key");
    return "sk_" + UUID.randomUUID().toString().replace("-", "")
        + UUID.randomUUID().toString().replace("-", "");
  }

}
