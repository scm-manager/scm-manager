package sonia.scm.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sonia.scm.ExceptionWithContext;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ContextualFallbackExceptionMapper implements ExceptionMapper<ExceptionWithContext> {

  private static final Logger logger = LoggerFactory.getLogger(ContextualFallbackExceptionMapper.class);

  @Override
  public Response toResponse(ExceptionWithContext exception) {
    logger.warn("mapping unexpected {} to status code 500", exception.getClass().getName(), exception);
    ErrorDto errorDto = new ErrorDto();
    errorDto.setMessage(exception.getMessage());
    errorDto.setContext(exception.getContext());
    errorDto.setErrorCode(exception.getCode());
    errorDto.setTransactionId(MDC.get("transaction_id"));
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity(errorDto)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
