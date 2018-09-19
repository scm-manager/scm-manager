package sonia.scm.api.v2.resources;

import sonia.scm.user.InvalidPasswordException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidPasswordExceptionMapper implements ExceptionMapper<InvalidPasswordException> {
  @Override
  public Response toResponse(InvalidPasswordException exception) {
    return Response.status(Response.Status.UNAUTHORIZED)
      .entity(exception.getMessage())
      .build();
  }
}
