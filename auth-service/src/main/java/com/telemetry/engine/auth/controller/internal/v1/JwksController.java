package com.telemetry.engine.auth.controller.internal.v1;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.auth.security.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class JwksController {

  private final JwtService jwtService;

  @GetMapping("/internal/v1/.well-known/jwks.json")
  public Map<String, Object> getJwks() {
    return jwtService.getJwks();
  }

}
