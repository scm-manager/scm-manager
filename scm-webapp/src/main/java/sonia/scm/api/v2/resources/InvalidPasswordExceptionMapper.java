package sonia.scm.api.v2.resources;

import sonia.scm.api.rest.ContextualExceptionMapper;
import sonia.scm.user.InvalidPasswordException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidPasswordExceptionMapper extends ContextualExceptionMapper<InvalidPasswordException> {

  @Inject
  public InvalidPasswordExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    super(InvalidPasswordException.class, Response.Status.BAD_REQUEST, mapper);
  }
}
