package sonia.scm.api.rest;

import sonia.scm.repository.RepositoryAlreadyExistsException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RepositoryAlreadyExistsExceptionMapper implements ExceptionMapper<RepositoryAlreadyExistsException> {
  @Override
  public Response toResponse(RepositoryAlreadyExistsException exception) {
    return Response.status(Status.CONFLICT).build();
  }
}
