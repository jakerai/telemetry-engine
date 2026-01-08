package com.telemetry.engine.auth.security.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemetry.engine.auth.security.jwt.filter.JwtSecurityFilter;
import com.telemetry.engine.common.constansts.ApiEndpointsConstants;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import com.telemetry.engine.common.utils.ResponseBuilder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final JwtSecurityFilter requestFilter;
  private final ObjectMapper objectMapper;
  private final JwtDecoder jwtDecoder;

  public SecurityConfig(JwtDecoder jwtDecoder, JwtSecurityFilter requestFilter,
      ObjectMapper objectMapper) {
    this.jwtDecoder = jwtDecoder;
    this.requestFilter = requestFilter;
    this.objectMapper = objectMapper;

  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.disable()).csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            (authz) -> authz.requestMatchers(ApiEndpointsConstants.ALL_PUBLIC_INTERNAL_ENDPOINTS).permitAll()
                .anyRequest().authenticated())
        .sessionManagement((sessionManagement) -> {
          sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        })
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(
            jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter()))
            .authenticationEntryPoint(jwtAuthenticationEntryPoint(objectMapper)))
        .addFilterAfter(requestFilter, BearerTokenAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();

    authConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
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
      return authorities;
    });
    return authConverter;
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }

  @Bean
  public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public AuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
    return (request, response, ex) -> {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");

      ServiceResponse<?> responseStatus =
          ResponseBuilder.error("Invalid or missing token", response.getStatus());

      response.getWriter().write(objectMapper.writeValueAsString(responseStatus));
    };
  }

}
