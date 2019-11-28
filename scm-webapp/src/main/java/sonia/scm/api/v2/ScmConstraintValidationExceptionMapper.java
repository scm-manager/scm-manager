package sonia.scm.api.v2;

import sonia.scm.ScmConstraintViolationException;
import sonia.scm.api.v2.resources.ScmViolationExceptionToErrorDtoMapper;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ScmConstraintValidationExceptionMapper implements ExceptionMapper<ScmConstraintViolationException> {

  private final ScmViolationExceptionToErrorDtoMapper mapper;

  @Inject
  public ScmConstraintValidationExceptionMapper(ScmViolationExceptionToErrorDtoMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Response toResponse(ScmConstraintViolationException exception) {
    return Response
      .status(Response.Status.BAD_REQUEST)
      .type(VndMediaType.ERROR_TYPE)
      .entity(mapper.map(exception))
      .build();
  }
}
