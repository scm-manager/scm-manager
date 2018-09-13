package sonia.scm.api.v2.resources;

import sonia.scm.api.rest.StatusExceptionMapper;
import sonia.scm.util.CRLFInjectionException;

import javax.ws.rs.core.Response;

public class CRLFInjectionExceptionMapper extends StatusExceptionMapper<CRLFInjectionException> {

  public CRLFInjectionExceptionMapper() {
    super(CRLFInjectionException.class, Response.Status.BAD_REQUEST);
  }
}
