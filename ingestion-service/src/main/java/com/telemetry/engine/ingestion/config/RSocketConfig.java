package com.telemetry.engine.ingestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.protobuf.ProtobufDecoder;
import org.springframework.http.codec.protobuf.ProtobufEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

@Configuration
public class RSocketConfig {

  @Bean
  public RSocketMessageHandler messageHandler() {
    RSocketMessageHandler handler = new RSocketMessageHandler();
    handler.setRSocketStrategies(
        RSocketStrategies.builder().encoders(encoders -> encoders.add(new ProtobufEncoder()))
            .decoders(decoders -> decoders.add(new ProtobufDecoder())).build());
    return handler;
  }

}
