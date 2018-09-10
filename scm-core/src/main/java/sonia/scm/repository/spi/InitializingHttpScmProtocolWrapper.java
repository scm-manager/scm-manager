package sonia.scm.repository.spi;

import javax.inject.Provider;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class InitializingHttpScmProtocolWrapper implements HttpScmProtocol {

  private final Provider<? extends HttpServlet> delegateProvider;

  private volatile boolean isInitialized = false;


  protected InitializingHttpScmProtocolWrapper(Provider<? extends HttpServlet> delegateProvider) {
    this.delegateProvider = delegateProvider;
  }

  @Override
  public void serve(HttpServletRequest request, HttpServletResponse response, ServletConfig config) throws ServletException, IOException {
    if (!isInitialized) {
      synchronized (this) {
        if (!isInitialized) {
          HttpServlet httpServlet = delegateProvider.get();
          initializeServlet(config, httpServlet);
          isInitialized = true;
        }
      }
    }
    delegateProvider.get().service(request, response);
  }

  protected void initializeServlet(ServletConfig config, HttpServlet httpServlet) throws ServletException {
    httpServlet.init(config);
  }
}
