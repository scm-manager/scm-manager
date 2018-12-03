package sonia.scm.security;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint(multi = false)
public interface JwtAccessTokenRefreshStrategy {
  boolean shouldBeRefreshed(JwtAccessToken oldToken);
}
