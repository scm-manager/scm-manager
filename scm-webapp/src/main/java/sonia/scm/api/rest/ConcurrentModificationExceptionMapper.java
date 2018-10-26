package sonia.scm.api.rest;

import sonia.scm.ConcurrentModificationException;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ConcurrentModificationExceptionMapper extends ContextualExceptionMapper<ConcurrentModificationException> {
  @Inject
  public ConcurrentModificationExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    super(ConcurrentModificationException.class, Response.Status.CONFLICT, mapper);
  }
}
