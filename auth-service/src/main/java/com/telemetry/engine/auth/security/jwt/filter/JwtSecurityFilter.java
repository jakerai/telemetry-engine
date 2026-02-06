package com.telemetry.engine.auth.security.jwt.filter;

import java.io.IOException;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.telemetry.engine.auth.security.model.AuthenticatedUser;
import com.telemetry.engine.common.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtSecurityFilter extends OncePerRequestFilter {

  private final UserDetailsService userDetailsService;
  private final JwtDecoder jwtDecoder;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    RequestContext.setClientIp(getClientIp(request));
    String path = request.getRequestURI();
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth && jwtAuth.isAuthenticated()) {
      
      authenticateWithToken(jwtAuth.getToken());
    } else if ("/api/external/v1/users/me/profile".equals(path)) {
     
      Optional<String> accessTokenOpt = getAccessTokenFromCookie(request);
      if (accessTokenOpt.isPresent()) {
        authenticateWithToken(accessTokenOpt.get());
      }
    }

    filterChain.doFilter(request, response);
  }

  private void authenticateWithToken(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    authenticateWithToken(jwt);
  }

  private void authenticateWithToken(Jwt jwt) {
    String username = jwt.getSubject();
    AuthenticatedUser authenticatedUser =
        (AuthenticatedUser) userDetailsService.loadUserByUsername(username);
    authenticatedUser.setCurrentJwt(jwt);

    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        authenticatedUser, null, authenticatedUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  /**
   * Extracts the value of the ACCESS_TOKEN cookie from the request.
   *
   * @param request HttpServletRequest containing cookies
   * @return the access token if present, otherwise empty
   */
  public static Optional<String> getAccessTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    for (Cookie cookie : request.getCookies()) {
      if ("ACCESS_TOKEN".equals(cookie.getName())) {
        return Optional.of(cookie.getValue());
      }
    }

    return Optional.empty();
  }


  private String getClientIp(HttpServletRequest request) {
    String[] headerCandidates =
        {"X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP", "True-Client-IP", "X-Client-IP",
            "X-Cluster-Client-IP", "Forwarded", "Forwarded-For", "X-Forwarded", "X-Forwarded-Host"};

    for (String header : headerCandidates) {
      String ip = request.getHeader(header);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        if (ip.contains(",")) {
          ip = ip.split(",")[0].trim();
        }
        return ip;
      }
    }
    return request.getRemoteAddr();
  }

}
