package sonia.scm.repository.spi;

import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.web.filter.PermissionFilter;

import javax.inject.Provider;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class InitializingHttpScmProtocolWrapper {

  private final Provider<? extends ScmProviderHttpServlet> delegateProvider;
  private final Provider<? extends PermissionFilter> permissionFilterProvider;
  private final Provider<ScmPathInfoStore> uriInfoStore;
  private final ScmConfiguration scmConfiguration;

  private volatile boolean isInitialized = false;


  protected InitializingHttpScmProtocolWrapper(Provider<? extends ScmProviderHttpServlet> delegateProvider, Provider<? extends PermissionFilter> permissionFilterProvider, Provider<ScmPathInfoStore> uriInfoStore, ScmConfiguration scmConfiguration) {
    this.delegateProvider = delegateProvider;
    this.permissionFilterProvider = permissionFilterProvider;
    this.uriInfoStore = uriInfoStore;
    this.scmConfiguration = scmConfiguration;
  }

  protected void initializeServlet(ServletConfig config, ScmProviderHttpServlet httpServlet) throws ServletException {
    httpServlet.init(config);
  }

  public HttpScmProtocol get(Repository repository) {
    return new ProtocolWrapper(repository);
  }

  private String computeBasePath() {
    if (uriInfoStore.get() != null && uriInfoStore.get().get() != null) {
      return uriInfoStore.get().get().getApiRestUri().resolve("../..").toASCIIString();
    } else {
      return scmConfiguration.getBaseUrl();
    }
  }

  private class ProtocolWrapper extends HttpScmProtocol {

    public ProtocolWrapper(Repository repository) {
      super(repository, computeBasePath());
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
