package sonia.scm.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ExceptionWithContext;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ContextualExceptionMapper<E extends ExceptionWithContext> implements ExceptionMapper<E> {

  private static final Logger logger = LoggerFactory.getLogger(ContextualExceptionMapper.class);

  private final ExceptionWithContextToErrorDtoMapper mapper;

  private final Response.Status status;
  private final Class<E> type;

  public ContextualExceptionMapper(Class<E> type, Response.Status status, ExceptionWithContextToErrorDtoMapper mapper) {
    this.mapper = mapper;
    this.type = type;
    this.status = status;
  }

  @Override
  public Response toResponse(E exception) {
    if (logger.isTraceEnabled()) {
      logger.trace("map {} to status code {}", type.getSimpleName(), status.getStatusCode(), exception);
    } else {
      logger.debug("map {} to status code {} with message '{}'", type.getSimpleName(), status.getStatusCode(), exception.getMessage());
    }
    return Response.status(status)
      .entity(mapper.map(exception))
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
