package sonia.scm.api.rest;

import sonia.scm.ConcurrentModificationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConcurrentModificationExceptionMapper implements ExceptionMapper<ConcurrentModificationException> {
  @Override
  public Response toResponse(ConcurrentModificationException exception) {
    return Response.status(Response.Status.CONFLICT).build();
  }
}
