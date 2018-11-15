package sonia.scm.api.rest;

import org.apache.shiro.authc.AuthenticationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper extends StatusExceptionMapper<AuthenticationException> {
  public AuthenticationExceptionMapper() {
    super(AuthenticationException.class, Response.Status.UNAUTHORIZED);
  }
}
