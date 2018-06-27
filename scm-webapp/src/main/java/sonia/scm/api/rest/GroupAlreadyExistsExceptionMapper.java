package sonia.scm.api.rest;

import sonia.scm.group.GroupAlreadyExistsException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GroupAlreadyExistsExceptionMapper implements ExceptionMapper<GroupAlreadyExistsException> {
  @Override
  public Response toResponse(GroupAlreadyExistsException exception) {
    return Response.status(Status.CONFLICT).build();
  }
}
