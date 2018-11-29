package sonia.scm.security;

public interface JwtAccessTokenRefreshStrategy {
  boolean shouldBeRefreshed(JwtAccessToken oldToken);
}
