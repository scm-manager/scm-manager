package sonia.scm.api.v2;

import org.jboss.resteasy.api.validation.ResteasyViolationException;
import sonia.scm.api.v2.resources.ViolationExceptionToErrorDtoMapper;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {

  private final ViolationExceptionToErrorDtoMapper mapper;

  @Inject
  public ValidationExceptionMapper(ViolationExceptionToErrorDtoMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Response toResponse(ResteasyViolationException exception) {
    return Response
      .status(Response.Status.BAD_REQUEST)
      .type(MediaType.APPLICATION_JSON_TYPE)
      .entity(mapper.map(exception))
      .build();
  }
}
