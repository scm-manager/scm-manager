package sonia.scm.api.rest;

import sonia.scm.AlreadyExistsException;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@Provider
public class AlreadyExistsExceptionMapper extends ContextualExceptionMapper<AlreadyExistsException> {
  @Inject
  public AlreadyExistsExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    super(AlreadyExistsException.class, Status.CONFLICT, mapper);
  }
}
