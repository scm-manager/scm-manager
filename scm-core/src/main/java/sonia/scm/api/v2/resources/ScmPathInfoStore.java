package sonia.scm.api.v2.resources;

import javax.ws.rs.core.UriInfo;

public class ScmPathInfoStore {

  private ScmPathInfo pathInfo;

  public ScmPathInfo get() {
    return pathInfo;
  }

  public void setFromRestRequest(UriInfo uriInfo) {
    if (this.pathInfo != null) {
      throw new IllegalStateException("UriInfo already set");
    }
    this.pathInfo = uriInfo::getBaseUri;
  }

}
