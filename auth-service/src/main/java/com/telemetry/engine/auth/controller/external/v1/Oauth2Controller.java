package com.telemetry.engine.auth.controller.external.v1;

import java.io.IOException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.telemetry.engine.auth.core.identity.dto.response.LoginResponse;
import com.telemetry.engine.auth.core.identity.service.IdentityService;
import com.telemetry.engine.auth.util.RedirectUriValidator;
import com.telemetry.engine.common.dto.response.ServiceResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/oauth2")
@RestController
public class Oauth2Controller {

  private final IdentityService identityService;
  private final RedirectUriValidator redirectUriValidator;


  /**
   * <p>
   * Entry point for OAuth2 login (web or mobile).
   * </p>
   * 
   * <p>
   * This method stores the <code>redirect_uri</code> in the session before starting the OAuth2
   * flow. The <code>redirect_uri</code> determines where the user will be sent after a successful
   * login.
   * </p>
   * 
   * <h3>Web Redirect</h3>
   * <ul>
   * <li>After login, the user is redirected to a web page, e.g.:
   * 
   * <pre>
   * http://localhost:5000/api/oauth2/start/google?redirect_uri=https://web.com/dashboard
   * </pre>
   * 
   * </li>
   * <li>This ensures the user returns to the page they were originally trying to access.</li>
   * </ul>
   * 
   * <h3>Mobile Redirect</h3>
   * <ul>
   * <li>Mobile apps use a custom URL scheme (or deep link), e.g.:
   * 
   * <pre>
   * myapp://oauth/success
   * </pre>
   * 
   * </li>
   * <li>This allows the operating system to open the app and navigate the user to the appropriate
   * screen (like dashboard or profile) after login.</li>
   * </ul>
   * 
   * <h3>Examples</h3>
   * <ul>
   * <li>Web: <code>https://web.com/dashboard</code></li>
   * <li>Mobile: <code>myapp://oauth/success</code></li>
   * </ul>
   */

  @GetMapping("/start/{provider}")
  public void startOauth2(@PathVariable String provider,
      @RequestParam("redirect_uri") String redirectUri, HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    redirectUri = redirectUriValidator.validateAndNormalize(redirectUri);

    /* Storing redirect URI in session */
    request.getSession().setAttribute("redirect_uri", redirectUri);

    /* Redirect to Spring Security OAuth2 authorization URL */
    response.sendRedirect("/oauth2/authorization/" + provider);
  }


  /**
   * <p>
   * This API handles OAuth2 login success for both web and mobile clients.
   * </p>
   *
   * <p>
   * <strong>Note:</strong> This endpoint is not called directly by the client; it is invoked
   * automatically after the user successfully authenticates with an external OAuth2 provider (e.g.,
   * Google, Facebook).
   * </p>
   *
   * <p>
   * <b>Purpose:</b> Completes the OAuth2 login flow by:
   * <ul>
   * <li>Creating or updating the user in the database.</li>
   * <li>Generating a JWT access token.</li>
   * <li>Storing the JWT in an <code>HttpOnly</code> cookie (for web clients).</li>
   * <li>Redirecting the user to the frontend page (web) or mobile app deep link.</li>
   * </ul>
   * </p>
   *
   * <p>
   * <b>Flow:</b>
   * </p>
   * <ol>
   * <li>User clicks login in the web or mobile app and is redirected to the OAuth2 provider:
   * <ul>
   * <li>Web:
   * <code>/oauth2/authorization/google?redirect_uri=http://localhost:3000/dashboard.html</code>
   * </li>
   * <li>Mobile: <code>/oauth2/authorization/google?redirect_uri=myapp://dashboard</code></li>
   * </ul>
   * </li>
   * <li>Backend stores <code>redirect_uri</code> in the session.</li>
   * <li>OAuth2 provider authenticates the user and redirects back to this endpoint.</li>
   * <li>This method:
   * <ul>
   * <li>Retrieves the <code>redirect_uri</code> from the session.</li>
   * <li>Creates or updates the user in the database and generates a JWT token.</li>
   * <li>Stores the JWT in an <code>HttpOnly</code> cookie (for web clients).</li>
   * <li>Redirects the user to the frontend page (web) or deep link (mobile).</li>
   * </ul>
   * </li>
   * </ol>
   *
   * <p>
   * <b>Security Notes:</b>
   * </p>
   * <ul>
   * <li>Redirect URIs are validated against a whitelist to prevent open redirect attacks.</li>
   * <li>Access token cookie is <code>HttpOnly</code> and should be <code>Secure</code> in
   * production (HTTPS).</li>
   * </ul>
   *
   * <p>
   * <b>Examples:</b>
   * </p>
   * <ul>
   * <li>Web redirect URI: <code>http://localhost:3000/dashboard.html</code></li>
   * <li>Android deep link: <code>my-android-app-schema-dev://mobile/dashboard</code></li>
   * <li>iOS deep link: <code>my-ios-app-schema-dev://mobile/dashboard</code></li>
   * </ul>
   *
   * @param oauth2User Authenticated OAuth2 user object populated by Spring Security.
   * @param authentication OAuth2 authentication token containing provider info.
   * @param request HTTP request, used to retrieve stored redirect URI from session.
   * @param response HTTP response, used to set cookie and perform redirect.
   * @throws IOException If redirect fails.
   */
  @GetMapping("/login/success")
  public void loginOauth2Web(@AuthenticationPrincipal OAuth2User oauth2User,
      OAuth2AuthenticationToken authentication, HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    /* Retrieving redirect URI from session or from state */
    String redirectUri = (String) request.getSession().getAttribute("redirect_uri");
    boolean isWeb = redirectUri.startsWith("http://") || redirectUri.startsWith("https://");

    /* Perform login + user creation + token generation */
    ServiceResponse<LoginResponse> serviceResponse =
        identityService.loginOauth2Web(oauth2User, authentication);
    if (isWeb) {
      /* Store token in HttpOnly cookie (recommended for web) */
      log.info("OAuth2 login successful for web: provider={} and redirecting to {}",
          authentication.getAuthorizedClientRegistrationId(), redirectUri);

      Cookie accessTokenCookie =
          new Cookie("ACCESS_TOKEN", serviceResponse.data().getAccessToken());
      accessTokenCookie.setHttpOnly(true);
      accessTokenCookie.setSecure(false); /* set true in production (HTTPS) */
      accessTokenCookie.setPath("/");
      accessTokenCookie.setMaxAge(60 * 60 * 24); // 1 day
      response.addCookie(accessTokenCookie);

    } else {
      /* Append token as query param to deep link (recommended for mobile) */
      String separator = redirectUri.contains("?") ? "&" : "?";
      redirectUri =
          redirectUri + separator + "access_token=" + serviceResponse.data().getAccessToken();

      log.info("OAuth2 login successful for mobile: redirecting to {}", redirectUri);
    }
    /* Redirect to user secure FRONTEND page after successful login */
    response.sendRedirect(redirectUri);
  }


}
