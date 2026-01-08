package com.telemetry.engine.auth.security.jwt.service.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.telemetry.engine.auth.security.jwt.config.JwtConfig;
import com.telemetry.engine.auth.security.jwt.enums.TokenType;
import com.telemetry.engine.auth.security.jwt.key.config.JwtProperties;
import com.telemetry.engine.auth.security.jwt.service.JwtService;
import com.telemetry.engine.auth.security.model.JwtToken;
import com.telemetry.engine.common.exception.TokenGenerationException;
import com.telemetry.engine.common.utils.SafeExtractUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

  private final JwtConfig jwtConfig;
  private final JwtProperties props;


  @Override
  public JwtToken generateAccessTokenOrThrow(String subject, Long userId, List<String> roles,
      List<String> permissions, Long refreshTokenId) {

    try {
      RSAKey rsaKey = jwtConfig.getCurrentRsaKey();
      log.info("generating token using = {}", rsaKey.getKeyID());
      if (rsaKey.toPrivateKey() == null) {
        log.error("RSA private key is not available for signing access token");
        throw new TokenGenerationException("Unable to generate token, please try again later");
      }

      JWSSigner signer = new RSASSASigner(rsaKey.toPrivateKey());
      Instant now = Instant.now();
      JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(subject).issuer(props.getIssuer())
          .issueTime(Date.from(now))
          .expirationTime(Date.from(now.plusSeconds(props.getAccessExpiry())))
          .claim("userId", userId).claim("roles", roles).claim("type", "ACCESS")
          .claim("permissions", permissions).claim("refreshTokenId", refreshTokenId).build();

      JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID())
          .type(JOSEObjectType.JWT).build();
      SignedJWT signedJWT = new SignedJWT(header, claims);
      signedJWT.sign(signer);
      return JwtToken.builder().value(signedJWT.serialize())
          .expirationTime(claims.getExpirationTime().toInstant()).expiresIn(props.getAccessExpiry())
          .build();
    } catch (JOSEException ex) {
      log.error("Failed to generate access token for userId={}", userId, ex);
      throw new TokenGenerationException("Unable to generate token, please try again later");
    }
  }

  @Override
  public JwtToken generateRefreshTokenOrThrow(String subject, Long userId) {
    try {
      RSAKey rsaKey = jwtConfig.getCurrentRsaKey();
      if (rsaKey.toPrivateKey() == null) {
        log.error("RSA private key is not available for signing refresh token");
        throw new TokenGenerationException("Unable to generate token, please try again later");
      }

      JWSSigner signer = new RSASSASigner(rsaKey.toPrivateKey());
      Instant now = Instant.now();
      JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(subject).issuer(props.getIssuer())
          .issueTime(Date.from(now))
          .expirationTime(Date.from(now.plusSeconds(props.getRefreshExpiry())))
          .claim("userId", userId).claim("type", "REFRESH").build();

      JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID())
          .type(JOSEObjectType.JWT).build();
      SignedJWT signedJWT = new SignedJWT(header, claims);
      signedJWT.sign(signer);

      return JwtToken.builder().value(signedJWT.serialize())
          .expirationTime(claims.getExpirationTime().toInstant())
          .expiresIn(props.getRefreshExpiry()).build();
    } catch (JOSEException ex) {
      log.error("Failed to generate refresh token for userId={}", userId, ex);
      throw new TokenGenerationException("Unable to generate token, please try again later");
    }
  }


  @Override
  public boolean validateRefreshToken(String token) {
    try {
      RSAKey rsaKey = jwtConfig.getCurrentRsaKey();
      SignedJWT jwt = SignedJWT.parse(token);
      JWSVerifier verifier = new RSASSAVerifier(rsaKey.toPublicJWK());

      JWTClaimsSet claims = jwt.getJWTClaimsSet();
      String type = SafeExtractUtil.safeGet(() -> claims.getStringClaim("type"), "");
      if (!jwt.verify(verifier) || !type.equalsIgnoreCase(TokenType.REFRESH.name()))
        return false;

      Date exp = jwt.getJWTClaimsSet().getExpirationTime();
      return exp != null && exp.after(new Date());
    } catch (Exception ex) {
      return false;
    }
  }

  @Override
  public Map<String, Object> getJwks() {
    RSAKey rsaKey = jwtConfig.getCurrentRsaKey();
    JWK publicKey = rsaKey.toPublicJWK();
    JWKSet jwkSet = new JWKSet(publicKey);
    return jwkSet.toJSONObject();
  }

}
