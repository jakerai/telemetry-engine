package com.telemetry.engine.gateway.security.apikey.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.telemetry.engine.common.dto.request.ServiceRequest;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.gateway.security.apikey.model.ApiKeyMeta;
import com.telemetry.engine.gateway.security.apikey.model.ApiKeyValidateRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class AuthServiceClient {

  private final WebClient webClient;

  public AuthServiceClient(@Value("${app.security.auth.service.base-url}") String baseUrl,
      WebClient.Builder builder) {
    this.webClient = builder.baseUrl(baseUrl).build();
  }

  public Mono<ApiKeyMeta> fetchApiKeyMeta(String apiKey) {
    log.info("Fetching apiKey={} from auth-service", apiKey);


    ApiKeyValidateRequest validateRequest = ApiKeyValidateRequest.builder().apiKey(apiKey).build();
    ServiceRequest<ApiKeyValidateRequest> serviceRequest = ServiceRequest.of(validateRequest);
    
    return webClient.post().uri("/api/internal/v1/api-keys/validate")
        .contentType(MediaType.APPLICATION_JSON).bodyValue(serviceRequest).retrieve()
        .bodyToMono(new ParameterizedTypeReference<ServiceResponse<ApiKeyMeta>>() {})
        .map(ServiceResponse::data) 
        .doOnNext(meta -> log.info("Found apiKey={} userId={}", apiKey, meta.getUserId()))
        .onErrorResume(ex -> {
          log.error("Error fetching apiKey={}", apiKey, ex);
          return Mono.empty();
        });
  }

}
