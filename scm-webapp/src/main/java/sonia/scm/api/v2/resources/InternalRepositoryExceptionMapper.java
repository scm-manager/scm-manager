package sonia.scm.api.v2.resources;

import sonia.scm.api.rest.StatusExceptionMapper;
import sonia.scm.repository.InternalRepositoryException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class InternalRepositoryExceptionMapper extends StatusExceptionMapper<InternalRepositoryException> {

  public InternalRepositoryExceptionMapper() {
    super(InternalRepositoryException.class, Response.Status.INTERNAL_SERVER_ERROR);
  }
}
