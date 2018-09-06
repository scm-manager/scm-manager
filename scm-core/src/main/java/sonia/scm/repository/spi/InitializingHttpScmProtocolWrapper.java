package sonia.scm.repository.spi;

import sonia.scm.repository.Repository;

import javax.inject.Provider;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;

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
          delegateProvider.get().init(config);
          isInitialized = true;
        }
      }
    }
    delegateProvider.get().service(request, response);
  }


  @Override
  public String getUrl(Repository repository, UriInfo uriInfo) {
    return uriInfo.getBaseUri().resolve(URI.create("../../repo/" + repository.getNamespace() + "/" + repository.getName())).toASCIIString();
  }
}
