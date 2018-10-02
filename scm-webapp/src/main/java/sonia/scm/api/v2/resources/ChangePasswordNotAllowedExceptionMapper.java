package sonia.scm.api.v2.resources;

import sonia.scm.user.ChangePasswordNotAllowedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ChangePasswordNotAllowedExceptionMapper implements ExceptionMapper<ChangePasswordNotAllowedException> {
  @Override
  public Response toResponse(ChangePasswordNotAllowedException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity(exception.getMessage())
      .build();
  }
}
