package com.telemetry.engine.ingestion.config;

import java.time.Duration;
//import org.springframework.boot.grpc.server.autoconfigure.GrpcServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

//@Configuration
public class GrpcConfig {

  /*@Bean
  public GrpcServerProperties grpcServerProperties() {
    GrpcServerProperties props = new GrpcServerProperties();

    props.setPort(9090);

    // 20 MB max message size
    props.setMaxInboundMessageSize(DataSize.ofMegabytes(20));

    GrpcServerProperties.KeepAlive keepAlive = props.getKeepAlive();

    keepAlive.setTime(Duration.ofSeconds(5)); // ping after 5s idle
    keepAlive.setTimeout(Duration.ofSeconds(20)); // wait 20s for ack
    keepAlive.setPermitWithoutCalls(true); // allow without active RPC
    keepAlive.setMaxIdle(Duration.ofMinutes(2)); // close idle conns
    keepAlive.setMaxAge(Duration.ofMinutes(10)); // rotate conns
    keepAlive.setMaxAgeGrace(Duration.ofSeconds(30)); // graceful close

    return props;
  }*/

}
