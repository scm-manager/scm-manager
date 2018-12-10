package sonia.scm.api.rest;

import sonia.scm.BadRequestException;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class BadRequestExceptionMapper extends ContextualExceptionMapper<BadRequestException> {
  @Inject
  public BadRequestExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    super(BadRequestException.class, Response.Status.BAD_REQUEST, mapper);
  }
}
