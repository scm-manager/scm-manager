package sonia.scm.web;

import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;
import sonia.scm.repository.spi.ScmProviderHttpServlet;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Enumeration;

@Singleton
@Extension
public class SvnScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {

  public static final String PARAMETER_SVN_PARENTPATH = "SVNParentPath";

  @Override
  public String getType() {
    return SvnRepositoryHandler.TYPE_NAME;
  }

  @Inject
  public SvnScmProtocolProviderWrapper(SvnDAVServletProvider servletProvider, Provider<ScmPathInfoStore> uriInfoStore, ScmConfiguration scmConfiguration) {
    super(servletProvider, uriInfoStore, scmConfiguration);
  }

  @Override
  protected void initializeServlet(ServletConfig config, ScmProviderHttpServlet httpServlet) throws ServletException {

    super.initializeServlet(new SvnConfigEnhancer(config), httpServlet);
  }

  private static class SvnConfigEnhancer implements ServletConfig {

    private final ServletConfig originalConfig;

    private SvnConfigEnhancer(ServletConfig originalConfig) {
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
    /**
     * Overridden to return the systems temp directory for the key {@link PARAMETER_SVN_PARENTPATH}.
     */
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
