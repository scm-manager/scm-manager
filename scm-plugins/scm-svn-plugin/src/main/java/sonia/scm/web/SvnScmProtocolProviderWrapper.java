package sonia.scm.web;

import sonia.scm.api.v2.resources.UriInfoStore;
import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Enumeration;

import static sonia.scm.web.SvnServletModule.PARAMETER_SVN_PARENTPATH;

@Singleton
public class SvnScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {
  @Inject
  public SvnScmProtocolProviderWrapper(Provider<SvnDAVServlet> servletProvider, Provider<SvnPermissionFilter> permissionFilter, Provider<UriInfoStore> uriInfoStore) {
    super(servletProvider, permissionFilter, uriInfoStore);
  }

  @Override
  protected void initializeServlet(ServletConfig config, HttpServlet httpServlet) throws ServletException {

    super.initializeServlet(new X(config), httpServlet);
  }

  private static class X implements ServletConfig {

    private final ServletConfig originalConfig;

    private X(ServletConfig originalConfig) {
      this.originalConfig = originalConfig;
    }

    @Override
    public String getServletName() {
      return originalConfig.getServletName();
    }

    @Override
    public ServletContext getServletContext() {
      return originalConfig.getServletContext();
    }

    @Override
    public String getInitParameter(String key) {
      if (PARAMETER_SVN_PARENTPATH.equals(key)) {
        return System.getProperty("java.io.tmpdir");
      }
      return originalConfig.getInitParameter(key);
    }

    @Override
    public Enumeration getInitParameterNames() {
      return originalConfig.getInitParameterNames();
    }
  }
}
