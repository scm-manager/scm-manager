package sonia.scm.api.rest;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedExceptionMapper extends StatusExceptionMapper<NotAuthorizedException> {
  public NotAuthorizedExceptionMapper()
  {
    super(NotAuthorizedException.class, Response.Status.UNAUTHORIZED);
  }
}
