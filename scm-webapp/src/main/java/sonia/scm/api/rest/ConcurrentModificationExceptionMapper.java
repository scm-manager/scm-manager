package sonia.scm.api.rest;

import sonia.scm.ConcurrentModificationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ConcurrentModificationExceptionMapper extends ContextualExceptionMapper<ConcurrentModificationException> {
  public ConcurrentModificationExceptionMapper() {
    super(ConcurrentModificationException.class, Response.Status.CONFLICT);
  }
}
