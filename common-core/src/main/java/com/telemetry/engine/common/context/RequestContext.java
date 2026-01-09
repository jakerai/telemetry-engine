package com.telemetry.engine.common.context;

import io.opentelemetry.api.trace.Span;

/**
 * RequestContext provides a lightweight way to store request-scoped data (such as the authenticated
 * user Id and client IP address) during the lifecycle of a single HTTP request.
 * <p>
 * This class uses {@link ThreadLocal} to ensure that data stored here is isolated per request
 * thread and is not shared across concurrent requests.
 * <p>
 * Important notes: - Never store sensitive or large objects in this context. Avoid passwords, JWT
 * tokens, request bodies, DTOs, entities, files, or anything large. - Values should be set inside
 * an authentication filter or request filter. - Values can then be accessed anywhere within the
 * request-handling flow. - {@link #clear()} MUST be called in a finally block after request
 * processing completes. This is critical when using a thread pool, because stale values can leak
 * into subsequent requests handled by the same thread.
 *
 * 
 * @author Vishal Rai
 * @version 1.0
 * @since 2025
 * @see com.rental.app.security.jwt.filter.JwtSecurityFilter
 */
public class RequestContext {

  private static final ThreadLocal<Long> userId = new ThreadLocal<>();
  private static final ThreadLocal<String> clientIp = new ThreadLocal<>();
  private static final ThreadLocal<Long> refreshTokenId = new ThreadLocal<>();

  public static void setUserId(Long id) {
    userId.set(id);
  }

  public static Long getUserId() {
    return userId.get();
  }

  public static void setClientIp(String ip) {
    clientIp.set(ip);
  }

  public static String getClientIp() {
    return clientIp.get();
  }

  public static void setRefreshTokenId(Long id) {
    refreshTokenId.set(id);
  }

  public static Long getRefreshTokenId() {
    return refreshTokenId.get();
  }

  /**
   * Clears all stored request-scoped values from the ThreadLocal variables.
   *
   * This method MUST be called at the end of every request, ideally inside a 'finally' block within
   * a filter. Since Spring uses a thread pool, the same thread may handle multiple requests.
   * Failing to clear the ThreadLocal values can cause data from a previous request (such as another
   * user's ID or IP address) to leak into the next request processed by the same thread.
   *
   * Proper cleanup is critical for preventing security issues, data leakage, and hard-to-debug
   * inconsistencies in your application.
   */
  public static void clear() {
    userId.remove();
    clientIp.remove();
    refreshTokenId.remove();
  }

  public static String getTraceId() {
    Span currentSpan = Span.current();
    return (currentSpan != null && currentSpan.getSpanContext().isValid())
        ? currentSpan.getSpanContext().getTraceId()
        : "no-trace-id";
  }

}
