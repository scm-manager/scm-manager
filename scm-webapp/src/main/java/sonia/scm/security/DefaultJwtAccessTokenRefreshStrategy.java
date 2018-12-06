package sonia.scm.security;

import sonia.scm.plugin.Extension;

@Extension
public class DefaultJwtAccessTokenRefreshStrategy extends PercentageJwtAccessTokenRefreshStrategy {
  public DefaultJwtAccessTokenRefreshStrategy() {
    super(0.5F);
  }
}
