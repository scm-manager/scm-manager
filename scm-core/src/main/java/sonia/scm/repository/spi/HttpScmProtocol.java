package sonia.scm.repository.spi;

import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ScmProtocol;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;

public abstract class HttpScmProtocol implements ScmProtocol {

  private final Repository repository;
  private final UriInfo uriInfo;

  public HttpScmProtocol(Repository repository, UriInfo uriInfo) {
    this.repository = repository;
    this.uriInfo = uriInfo;
  }

  @Override
  public String getType() {
    return "http";
  }

  @Override
  public String getUrl() {
    return uriInfo.getBaseUri().resolve(URI.create("../../repo/" + repository.getNamespace() + "/" + repository.getName())).toASCIIString();
  }

  public abstract void serve(HttpServletRequest request, HttpServletResponse response, ServletConfig config) throws ServletException, IOException;
}
