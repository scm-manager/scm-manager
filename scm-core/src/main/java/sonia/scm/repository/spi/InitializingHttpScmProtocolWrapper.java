package sonia.scm.repository.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.UriInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.web.filter.PermissionFilter;

import javax.inject.Provider;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class InitializingHttpScmProtocolWrapper {

  private static final Logger logger =
    LoggerFactory.getLogger(InitializingHttpScmProtocolWrapper.class);

  private final Provider<? extends ScmProviderHttpServlet> delegateProvider;
  private final Provider<? extends PermissionFilter> permissionFilterProvider;
  private final Provider<UriInfoStore> uriInfoStore;

  private volatile boolean isInitialized = false;


  protected InitializingHttpScmProtocolWrapper(Provider<? extends ScmProviderHttpServlet> delegateProvider, Provider<? extends PermissionFilter> permissionFilterProvider, Provider<UriInfoStore> uriInfoStore) {
    this.delegateProvider = delegateProvider;
    this.permissionFilterProvider = permissionFilterProvider;
    this.uriInfoStore = uriInfoStore;
  }

  protected void initializeServlet(ServletConfig config, ScmProviderHttpServlet httpServlet) throws ServletException {
    httpServlet.init(config);
  }

  public HttpScmProtocol get(Repository repository) {
    return new ProtocolWrapper(repository);
  }

  private class ProtocolWrapper extends HttpScmProtocol {

    public ProtocolWrapper(Repository repository) {
      super(repository, uriInfoStore.get().get());
    }

    @Override
    protected void serve(HttpServletRequest request, HttpServletResponse response, Repository repository, ServletConfig config) throws ServletException, IOException {
      if (!isInitialized) {
        synchronized (InitializingHttpScmProtocolWrapper.this) {
          if (!isInitialized) {
            ScmProviderHttpServlet httpServlet = delegateProvider.get();
            initializeServlet(config, httpServlet);
            isInitialized = true;
          }
        }
      }

      permissionFilterProvider.get().executeIfPermitted(
        request,
        response,
        repository,
        () -> delegateProvider.get().service(request, response, repository));
    }
  }
}
