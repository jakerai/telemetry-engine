package com.telemetry.engine.auth.security.jwt.filter;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.telemetry.engine.auth.security.AuthenticatedUser;
import com.telemetry.engine.common.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtSecurityFilter extends OncePerRequestFilter {

  @Autowired
  private UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    RequestContext.setClientIp(getClientIp(request));


    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth instanceof JwtAuthenticationToken jwtAuth && auth.isAuthenticated()) {
      String username = jwtAuth.getToken().getSubject();

      AuthenticatedUser authenticatedUser =
          (AuthenticatedUser) userDetailsService.loadUserByUsername(username);
      authenticatedUser.setCurrentJwt(jwtAuth.getToken());

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          authenticatedUser, null, authenticatedUser.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
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
