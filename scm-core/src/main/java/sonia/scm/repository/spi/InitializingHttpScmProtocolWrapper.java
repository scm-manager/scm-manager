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
    
package sonia.scm.repository.spi;

import lombok.extern.slf4j.Slf4j;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ScmProtocolProvider;

import javax.inject.Provider;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Slf4j
public abstract class InitializingHttpScmProtocolWrapper implements ScmProtocolProvider<HttpScmProtocol> {

  private final Provider<? extends ScmProviderHttpServlet> delegateProvider;
  private final Provider<ScmPathInfoStore> pathInfoStore;
  private final ScmConfiguration scmConfiguration;

  private volatile boolean isInitialized = false;


  protected InitializingHttpScmProtocolWrapper(Provider<? extends ScmProviderHttpServlet> delegateProvider, Provider<ScmPathInfoStore> pathInfoStore, ScmConfiguration scmConfiguration) {
    this.delegateProvider = delegateProvider;
    this.pathInfoStore = pathInfoStore;
    this.scmConfiguration = scmConfiguration;
  }

  protected void initializeServlet(ServletConfig config, ScmProviderHttpServlet httpServlet) throws ServletException {
    httpServlet.init(config);
  }

  @Override
  public HttpScmProtocol get(Repository repository) {
    if (!repository.getType().equals(getType())) {
      throw new IllegalArgumentException(String.format("cannot handle repository with type %s with protocol for type %s", repository.getType(), getType()));
    }
    return new ProtocolWrapper(repository, computeBasePath());
  }

  private String computeBasePath() {
    return getPathFromScmPathInfoIfAvailable().orElse(getPathFromConfiguration());
  }

  private Optional<String> getPathFromScmPathInfoIfAvailable() {
    try {
      ScmPathInfoStore scmPathInfoStore = pathInfoStore.get();
      if (scmPathInfoStore != null && scmPathInfoStore.get() != null) {
        return of(scmPathInfoStore.get().getRootUri().toASCIIString());
      }
    } catch (Exception e) {
      log.debug("could not get ScmPathInfoStore from context", e);
    }
    return empty();
  }

  private String getPathFromConfiguration() {
    log.debug("using base path from configuration: {}", scmConfiguration.getBaseUrl());
    return scmConfiguration.getBaseUrl();
  }

  private class ProtocolWrapper extends HttpScmProtocol {

    public ProtocolWrapper(Repository repository, String basePath) {
      super(repository, basePath);
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

      delegateProvider.get().service(request, response, repository);
    }

  }
}
