package sonia.scm.security;

import org.apache.shiro.authc.AuthenticationToken;

public class AnonymousToken implements AuthenticationToken {
  //Anonymous Token does not need an implementation
  @Override
  public Object getPrincipal() {
    return null;
  }

  @Override
  public Object getCredentials() {
    return null;
  }
}
