package sonia.scm.api.v2;

import org.jboss.resteasy.api.validation.ResteasyViolationException;
import sonia.scm.api.v2.resources.ResteasyViolationExceptionToErrorDtoMapper;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ResteasyValidationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {

  private final ResteasyViolationExceptionToErrorDtoMapper mapper;

  @Inject
  public ResteasyValidationExceptionMapper(ResteasyViolationExceptionToErrorDtoMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Response toResponse(ResteasyViolationException exception) {
    return Response
      .status(Response.Status.BAD_REQUEST)
      .type(VndMediaType.ERROR_TYPE)
      .entity(mapper.map(exception))
      .build();
  }
}
