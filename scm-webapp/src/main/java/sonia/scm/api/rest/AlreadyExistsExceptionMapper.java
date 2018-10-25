package sonia.scm.api.rest;

import sonia.scm.AlreadyExistsException;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AlreadyExistsExceptionMapper implements ExceptionMapper<AlreadyExistsException> {
  @Override
  public Response toResponse(AlreadyExistsException exception) {
    return Response.status(Status.CONFLICT)
      .entity(ErrorDto.from(exception))
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
