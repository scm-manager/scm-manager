package sonia.scm.api.rest;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class NotAllowedExceptionMapper extends StatusExceptionMapper<NotAllowedException> {
  public NotAllowedExceptionMapper() {
    super(NotAllowedException.class, Response.Status.METHOD_NOT_ALLOWED);
  }
}
