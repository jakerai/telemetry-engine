package com.telemetry.engine.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class ApiGatewayApplication {

  public static void main(String[] args) {
    // This is the glue that makes tracing work across reactive operators
    Hooks.enableAutomaticContextPropagation();
    SpringApplication.run(ApiGatewayApplication.class, args);
  }

}
