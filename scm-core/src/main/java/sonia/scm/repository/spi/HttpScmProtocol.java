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
  private final String basePath;

  public HttpScmProtocol(Repository repository, String basePath) {
    this.repository = repository;
    this.basePath = basePath;
  }

  @Override
  public String getType() {
    return "http";
  }

  @Override
  public String getUrl() {
      return URI.create(basePath + "/").resolve("repo/" + repository.getNamespace() + "/" + repository.getName()).toASCIIString();
  }

  public final void serve(HttpServletRequest request, HttpServletResponse response, ServletConfig config) throws ServletException, IOException {
    serve(request, response, repository, config);
  }

  protected abstract void serve(HttpServletRequest request, HttpServletResponse response, Repository repository, ServletConfig config) throws ServletException, IOException;
}
