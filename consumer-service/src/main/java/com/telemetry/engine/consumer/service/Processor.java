package com.telemetry.engine.consumer.service;

import reactor.core.publisher.Mono;

public interface Processor {

  Mono<Void> process(String jsonArrayValue);

}
