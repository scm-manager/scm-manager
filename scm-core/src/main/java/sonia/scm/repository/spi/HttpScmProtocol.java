package sonia.scm.repository.spi;

import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ScmProtocol;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public abstract class HttpScmProtocol implements ScmProtocol {

  private final Repository repository;

  public HttpScmProtocol(Repository repository) {
    this.repository = repository;
  }

  @Override
  public String getType() {
    return "http";
  }

  @Override
  public String getUrl(URI baseUri) {
    return baseUri.resolve(URI.create("repo" + "/" + repository.getNamespace() + "/" + repository.getName())).toASCIIString();
  }

  public abstract void serve(HttpServletRequest request, HttpServletResponse response, ServletConfig config) throws ServletException, IOException;
}
