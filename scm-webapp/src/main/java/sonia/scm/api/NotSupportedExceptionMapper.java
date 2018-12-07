package sonia.scm.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotSupportedExceptionMapper implements ExceptionMapper<NotSupportedException> {

  private static final Logger LOG = LoggerFactory.getLogger(NotSupportedExceptionMapper.class);

  @Override
  public Response toResponse(NotSupportedException exception) {
    LOG.debug("illegal media type");
    ErrorDto error = new ErrorDto();
    error.setTransactionId(MDC.get("transaction_id"));
    error.setMessage("illegal media type");
    error.setErrorCode("8pRBYDURx1");
    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
      .entity(error)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
