/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
