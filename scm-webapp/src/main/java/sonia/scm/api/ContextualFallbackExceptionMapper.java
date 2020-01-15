package sonia.scm.api;

import sonia.scm.ExceptionWithContext;
import sonia.scm.api.rest.ContextualExceptionMapper;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
public class ContextualFallbackExceptionMapper extends ContextualExceptionMapper<ExceptionWithContext> {

  @Inject
  public ContextualFallbackExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    super(ExceptionWithContext.class, INTERNAL_SERVER_ERROR, mapper);
  }
}
