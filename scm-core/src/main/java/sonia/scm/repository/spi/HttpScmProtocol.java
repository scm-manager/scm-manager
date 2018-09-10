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

public interface HttpScmProtocol extends ScmProtocol {
  @Override
  default String getType() {
    return "http";
  }

  @Override
  default String getUrl(Repository repository, UriInfo uriInfo) {
    return uriInfo.getBaseUri().resolve(URI.create("../../repo/" + repository.getNamespace() + "/" + repository.getName())).toASCIIString();
  }

  void serve(HttpServletRequest request, HttpServletResponse response, ServletConfig config) throws ServletException, IOException;
}
