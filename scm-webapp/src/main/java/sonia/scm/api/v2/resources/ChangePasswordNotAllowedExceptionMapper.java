package sonia.scm.api.v2.resources;

import sonia.scm.api.rest.ContextualExceptionMapper;
import sonia.scm.user.ChangePasswordNotAllowedException;
import sonia.scm.user.InvalidPasswordException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ChangePasswordNotAllowedExceptionMapper extends ContextualExceptionMapper<ChangePasswordNotAllowedException> {
  @Inject
  public ChangePasswordNotAllowedExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    super(ChangePasswordNotAllowedException.class, Response.Status.BAD_REQUEST, mapper);
  }
}
