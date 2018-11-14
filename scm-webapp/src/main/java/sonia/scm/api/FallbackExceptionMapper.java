package sonia.scm.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Collections;

@Provider
public class FallbackExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger logger = LoggerFactory.getLogger(FallbackExceptionMapper.class);

  private static final String ERROR_CODE = "CmR8GCJb31";

  private final ExceptionWithContextToErrorDtoMapper mapper;

  @Inject
  public FallbackExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Response toResponse(Exception exception) {
    logger.debug("map {} to status code 500", exception);
    ErrorDto errorDto = new ErrorDto();
    errorDto.setMessage("internal server error");
    errorDto.setContext(Collections.emptyList());
    errorDto.setErrorCode(ERROR_CODE);
    errorDto.setTransactionId(MDC.get("transaction_id"));
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity(errorDto)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
