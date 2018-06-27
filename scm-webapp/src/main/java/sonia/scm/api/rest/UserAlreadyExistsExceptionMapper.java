package sonia.scm.api.rest;

import sonia.scm.user.UserAlreadyExistsException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UserAlreadyExistsExceptionMapper implements ExceptionMapper<UserAlreadyExistsException> {
  @Override
  public Response toResponse(UserAlreadyExistsException exception) {
    return Response.status(Status.CONFLICT).build();
  }
}
