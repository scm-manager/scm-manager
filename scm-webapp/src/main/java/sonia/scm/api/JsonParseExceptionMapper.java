package sonia.scm.api;

import com.fasterxml.jackson.core.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Collections;

@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

  private static final Logger logger = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

  private static final String ERROR_CODE = "2VRCrvpL71";

  @Override
  public Response toResponse(JsonParseException exception) {
    logger.trace("got illegal json: {}", exception.getMessage());
    ErrorDto errorDto = new ErrorDto();
    errorDto.setMessage("illegal json content: " + exception.getMessage());
    errorDto.setContext(Collections.emptyList());
    errorDto.setErrorCode(ERROR_CODE);
    errorDto.setTransactionId(MDC.get("transaction_id"));
    return Response.status(Response.Status.BAD_REQUEST)
      .entity(errorDto)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
