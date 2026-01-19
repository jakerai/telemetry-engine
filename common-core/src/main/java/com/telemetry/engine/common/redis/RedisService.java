package com.telemetry.engine.common.redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import com.telemetry.engine.common.mapper.MapperService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RedisService {

  private final ReactiveRedisTemplate<String, String> redisTemplate;
  private final ReactiveRedisMessageListenerContainer listenerContainer;
  private final MapperService mapperService;

  
  public Mono<Boolean> putValue(String key, Object value, Duration ttl) {
    String json = mapperService.serializeToJson(value);
    return redisTemplate.opsForValue().set(key, json, ttl);
  }

  public <T> Mono<T> getValue(String key, Class<T> clazz) {
    return redisTemplate.opsForValue().get(key)
        .map(json -> mapperService.deserializeFromJson(json, clazz));
  }

  
  public Mono<Long> addToSet(String key, String... members) {
    return redisTemplate.opsForSet().add(key, members);
  }

  public Mono<Long> removeFromSet(String key, String... members) {
    return redisTemplate.opsForSet().remove(key, (Object[]) members);
  }

  public Flux<String> getSetMembers(String key) {
    return redisTemplate.opsForSet().members(key);
  }

  public Flux<String> unionSets(List<String> keys) {
    if (keys == null || keys.isEmpty())
      return Flux.empty();
    if (keys.size() == 1)
      return getSetMembers(keys.get(0));
    return redisTemplate.opsForSet().union(keys.get(0), keys.subList(1, keys.size()));
  }

  
  public Mono<Boolean> putHash(String key, Map<String, Object> fields) {
    // Converting all values to JSON strings
    Map<String, String> jsonFields = fields.entrySet().stream().collect(
        Collectors.toMap(Map.Entry::getKey, e -> mapperService.serializeToJson(e.getValue())));
    return redisTemplate.opsForHash().putAll(key, jsonFields);
  }

  public Mono<Map<String, Object>> getHash(String key) {
    return redisTemplate.opsForHash().entries(key).collectMap(entry -> entry.getKey().toString(),
        entry -> mapperService.deserializeFromJson(entry.getValue().toString(), Object.class));
  }

  public <T> Mono<T> getHashField(String key, String field, Class<T> clazz) {
    return redisTemplate.opsForHash().get(key, field)
        .map(value -> mapperService.deserializeFromJson(value.toString(), clazz));
  }
 
  public Mono<Long> publish(String topic, Object message) {
    String json = mapperService.serializeToJson(message);
    return redisTemplate.convertAndSend(topic, json);
  }

  public Flux<String> subscribeToChannel(String channel) {
    return listenerContainer.receive(ChannelTopic.of(channel))
        .map(ReactiveSubscription.Message::getMessage).map(Object::toString); // Returning JSON string
  }

}
