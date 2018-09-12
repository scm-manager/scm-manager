package sonia.scm.api.v2.resources;

public class ScmPathInfoStore {

  private ScmPathInfo pathInfo;

  public ScmPathInfo get() {
    return pathInfo;
  }

  public void set(ScmPathInfo info) {
    if (this.pathInfo != null) {
      throw new IllegalStateException("UriInfo already set");
    }
    this.pathInfo = info;
  }

}
