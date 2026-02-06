package com.telemetry.engine.auth.constants;

public class Oauth2ProviderEndpoints {

  private Oauth2ProviderEndpoints() {
    /* prevent instantiation */
  }

  public static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

  public static final String FACEBOOK_USERINFO_URL =
      "https://graph.facebook.com/me";
}
