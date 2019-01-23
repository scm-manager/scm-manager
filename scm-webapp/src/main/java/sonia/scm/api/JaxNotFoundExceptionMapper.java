package sonia.scm.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Collections;

@Provider
public class JaxNotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

  private static final Logger logger = LoggerFactory.getLogger(JaxNotFoundExceptionMapper.class);

  private static final String ERROR_CODE = "92RCCCMHO1";

  @Override
  public Response toResponse(NotFoundException exception) {
    logger.debug(exception.getMessage());
    ErrorDto errorDto = new ErrorDto();
    errorDto.setMessage("path not found");
    errorDto.setContext(Collections.emptyList());
    errorDto.setErrorCode(ERROR_CODE);
    errorDto.setTransactionId(MDC.get("transaction_id"));
    return Response.status(Response.Status.NOT_FOUND)
      .entity(errorDto)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
