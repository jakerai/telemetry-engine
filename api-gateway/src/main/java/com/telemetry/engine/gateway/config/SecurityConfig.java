package com.telemetry.engine.gateway.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemetry.engine.common.constansts.ApiEndpointsConstants;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  private final ObjectMapper objectMapper;
  private final ReactiveJwtDecoder jwtDecoder;

  public SecurityConfig(ReactiveJwtDecoder jwtDecoder, ObjectMapper objectMapper) {
    this.jwtDecoder = jwtDecoder;
    this.objectMapper = objectMapper;
  }


  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http.csrf(csrf -> csrf.disable())
        .authorizeExchange(
            exchanges -> exchanges.pathMatchers(ApiEndpointsConstants.ALL_PUBLIC_EXTERNAL_ENDPOINTS)
                .permitAll().anyExchange().authenticated())
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtDecoder(jwtDecoder)
                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
            .authenticationEntryPoint(buildJwtAuthEntryPoint(objectMapper)))
        .build();
  }


  @Bean
  public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
    return jwt -> {
      List<GrantedAuthority> authorities = new ArrayList<>();

      // Roles
      List<String> roles = jwt.getClaimAsStringList("roles");
      if (roles != null) {
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      }

      // Permissions
      List<String> permissions = jwt.getClaimAsStringList("permissions");
      if (permissions != null) {
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
      }

      // Build reactive token
      AbstractAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);
      return Mono.just(token);
    };
  }

  private ServerAuthenticationEntryPoint buildJwtAuthEntryPoint(ObjectMapper objectMapper) {
    return (exchange, ex) -> {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

      ServiceResponse<?> responseStatus = ServiceResponse.builder().message("Invalid or missing token")
          .status(exchange.getResponse().getStatusCode().value()).build();

      return exchange.getResponse().writeWith(Mono.fromSupplier(() -> {
        try {
          return exchange.getResponse().bufferFactory()
              .wrap(objectMapper.writeValueAsBytes(responseStatus));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }));
    };
  }

}
