package sonia.scm.api.v2.resources;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AutoCompleteBadParamExceptionMapper implements ExceptionMapper<AutoCompleteBadParamException> {

  @Override
  public Response toResponse(AutoCompleteBadParamException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity(exception.getMessage())
      .build();
  }
}
