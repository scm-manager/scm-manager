package sonia.scm.api.v2.resources;

import sonia.scm.api.rest.ContextualExceptionMapper;
import sonia.scm.repository.InternalRepositoryException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class InternalRepositoryExceptionMapper extends ContextualExceptionMapper<InternalRepositoryException> {

  @Inject
  public InternalRepositoryExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    super(InternalRepositoryException.class, Response.Status.INTERNAL_SERVER_ERROR, mapper);
  }
}
