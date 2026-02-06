package com.telemetry.engine.auth.util;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.telemetry.engine.auth.config.AppSecurityProperties;
import com.telemetry.engine.common.exception.InvalidRedirectUriException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectUriValidator {

  private final AppSecurityProperties properties;

  /**
   * Normalized allowed redirect URIs (cached for O(1) lookup)
   */
  private Set<String> allowedUris;

  @PostConstruct
  void init() {
    this.allowedUris = properties.getOauth2().getAllowedRedirectUris().stream().map(this::normalize)
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Validates the redirect URI and returns its normalized form.
   *
   * @throws InvalidRedirectUriException if URI is invalid or not whitelisted
   */
  public String validateAndNormalize(String requestedUri) {
    log.info("Original redirect to={} ", requestedUri);

    if (requestedUri == null || requestedUri.isBlank()) {
      throw new InvalidRedirectUriException("Redirect URI is missing");
    }

    try {
      String normalized = normalize(requestedUri);

      if (!allowedUris.contains(normalized)) {
        throw new InvalidRedirectUriException("Redirect URI is not allowed: " + requestedUri);
      }
      log.info("Normalized redirect uri={}", normalized);
      return normalized;
    } catch (IllegalArgumentException e) {
      throw new InvalidRedirectUriException("Invalid redirect URI format: " + requestedUri);
    }
  }

  /**
   * Normalizes a URI into a canonical string form: scheme://host[:port]/path
   */
  private String normalize(String uriStr) {
    URI uri = URI.create(uriStr);

    if (uri.getScheme() == null || uri.getHost() == null) {
      throw new IllegalArgumentException("Redirect URI must contain scheme and host");
    }

    String scheme = uri.getScheme().toLowerCase();
    String host = uri.getHost().toLowerCase();
    int port = uri.getPort();
    String path = normalizePath(uri.getPath());

    StringBuilder normalized = new StringBuilder();
    normalized.append(scheme).append("://").append(host);

    // Keep port flexible (important for dev environments)
    if (port != -1) {
      normalized.append(":").append(port);
    }

    normalized.append(path);
    return normalized.toString();
  }

  /**
   * Ensures path always starts with "/"
   */
  private String normalizePath(String path) {
    if (path == null || path.isBlank()) {
      return "/";
    }
    return path.startsWith("/") ? path : "/" + path;
  }

}
