/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import sonia.scm.RootURL;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;
import sonia.scm.repository.spi.ScmProviderHttpServlet;

import java.util.Enumeration;

@Singleton
@Extension
public class SvnScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {

  public static final String PARAMETER_SVN_PARENTPATH = "SVNParentPath";

  @Inject
  public SvnScmProtocolProviderWrapper(SvnDAVServletProvider servletProvider, RootURL rootURL) {
    super(servletProvider, rootURL);
  }

  @Override
  public String getType() {
    return SvnRepositoryHandler.TYPE_NAME;
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
