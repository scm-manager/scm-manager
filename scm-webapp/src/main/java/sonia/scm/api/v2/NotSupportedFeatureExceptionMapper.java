package sonia.scm.api.v2;

import sonia.scm.NotSupportedFeatureException;
import sonia.scm.api.rest.ContextualExceptionMapper;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class NotSupportedFeatureExceptionMapper extends ContextualExceptionMapper<NotSupportedFeatureException> {
  @Inject
  public NotSupportedFeatureExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    super(NotSupportedFeatureException.class, Response.Status.BAD_REQUEST, mapper);
  }
}
