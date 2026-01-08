package com.telemetry.engine.common.constansts;

/**
 * Constants class defining all public API endpoints that don't require authentication.
 * 
 * <p>
 * These endpoints are configured in Spring Security to permit all access without authentication.
 * Used primarily in SecurityConfig for request matchers.
 * </p>
 * 
 * @author Vishal Rai
 * @version 1.0
 * @since 2025
 */
public class ApiEndpointsConstants {

  private ApiEndpointsConstants() {}


/**
 * To access from public Internet
 */
  public static final String[] ALL_PUBLIC_EXTERNAL_ENDPOINTS =
      {"/api/external/v1/auth/signup", "/api/external/v1/auth/login",
          "/api/external/v1/auth/refresh-token", "/api/external/v1/auth/forgot-password",
          "/api/external/v1/auth/reset-password", "/api/external/v1/ingest", "/actuator/**"};


  private static final String[] INTERNAL_ONLY_ENDPOINTS =
      {"/internal/v1/.well-known/jwks.json", "/api/internal/v1/api-keys/validate"};


  /**
   * To access from service to service internally
   */
  public static final String[] ALL_PUBLIC_INTERNAL_ENDPOINTS;

  static {
    ALL_PUBLIC_INTERNAL_ENDPOINTS =
        new String[ALL_PUBLIC_EXTERNAL_ENDPOINTS.length + INTERNAL_ONLY_ENDPOINTS.length];

    System.arraycopy(ALL_PUBLIC_EXTERNAL_ENDPOINTS, 0, ALL_PUBLIC_INTERNAL_ENDPOINTS, 0,
        ALL_PUBLIC_EXTERNAL_ENDPOINTS.length);

    System.arraycopy(INTERNAL_ONLY_ENDPOINTS, 0, ALL_PUBLIC_INTERNAL_ENDPOINTS,
        ALL_PUBLIC_EXTERNAL_ENDPOINTS.length, INTERNAL_ONLY_ENDPOINTS.length);
  }

  public static void main(String args []) {
    System.out.println("internal="+ALL_PUBLIC_EXTERNAL_ENDPOINTS);
  }
}
