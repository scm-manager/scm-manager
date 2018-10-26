package sonia.scm.api.rest;

import sonia.scm.AlreadyExistsException;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

@Provider
public class AlreadyExistsExceptionMapper extends ContextualExceptionMapper<AlreadyExistsException> {
  public AlreadyExistsExceptionMapper() {
    super(AlreadyExistsException.class, Status.CONFLICT);
  }
}
