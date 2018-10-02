package sonia.scm.repository.spi;

import sonia.scm.repository.Repository;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ScmProviderHttpServlet {

  void service(HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException, IOException;

  void init(ServletConfig config) throws ServletException;
}
