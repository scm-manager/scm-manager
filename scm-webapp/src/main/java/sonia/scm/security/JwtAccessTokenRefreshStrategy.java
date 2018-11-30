package sonia.scm.security;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface JwtAccessTokenRefreshStrategy {
  boolean shouldBeRefreshed(JwtAccessToken oldToken);
}
