package com.telemetry.engine.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import com.telemetry.engine.gateway.security.apikey.model.ApiKeyMeta;

@Configuration
public class RedisConfig {

  
  @Bean
  public ReactiveRedisTemplate<String, ApiKeyMeta> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory factory) {

    Jackson2JsonRedisSerializer<ApiKeyMeta> valueSerializer =
        new Jackson2JsonRedisSerializer<>(ApiKeyMeta.class);

    RedisSerializationContext<String, ApiKeyMeta> context = RedisSerializationContext
        .<String, ApiKeyMeta>newSerializationContext()
        .key(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
        .value(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
        .hashKey(
            RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
        .hashValue(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
        .build();

    return new ReactiveRedisTemplate<>(factory, context);
  }

}
