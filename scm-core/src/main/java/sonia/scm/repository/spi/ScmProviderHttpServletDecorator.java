package sonia.scm.repository.spi;

import sonia.scm.repository.Repository;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ScmProviderHttpServletDecorator implements ScmProviderHttpServlet {

  private final ScmProviderHttpServlet object;

  public ScmProviderHttpServletDecorator(ScmProviderHttpServlet object) {
    this.object = object;
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException, IOException {
    object.service(request, response, repository);
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    object.init(config);
  }
}
