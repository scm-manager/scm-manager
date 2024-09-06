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

package sonia.scm.repository.spi;

import jakarta.inject.Provider;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.RootURL;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ScmProtocolProvider;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Slf4j
public abstract class InitializingHttpScmProtocolWrapper implements ScmProtocolProvider<HttpScmProtocol> {

  private final Provider<? extends ScmProviderHttpServlet> delegateProvider;
  private final Supplier<String> basePathSupplier;

  private volatile boolean isInitialized = false;

  /**
   * @deprecated use {@link InitializingHttpScmProtocolWrapper(Provider, RootURL)} instead.
   */
  @Deprecated
  protected InitializingHttpScmProtocolWrapper(Provider<? extends ScmProviderHttpServlet> delegateProvider, Provider<ScmPathInfoStore> pathInfoStore, ScmConfiguration scmConfiguration) {
    this.delegateProvider = delegateProvider;
    this.basePathSupplier = new LegacySupplier(pathInfoStore, scmConfiguration);
  }

  /**
   * @since 2.3.1
   */
  public InitializingHttpScmProtocolWrapper(Provider<? extends ScmProviderHttpServlet> delegateProvider, RootURL rootURL) {
    this.delegateProvider = delegateProvider;
    this.basePathSupplier = rootURL::getAsString;
  }

  protected void initializeServlet(ServletConfig config, ScmProviderHttpServlet httpServlet) throws ServletException {
    httpServlet.init(config);
  }

  @Override
  public HttpScmProtocol get(Repository repository) {
    if (!repository.getType().equals(getType())) {
      throw new IllegalArgumentException(
        String.format("cannot handle repository with type %s with protocol for type %s", repository.getType(), getType())
      );
    }
    return new ProtocolWrapper(repository, basePathSupplier.get());
  }

  private static class LegacySupplier implements Supplier<String> {

    private final Provider<ScmPathInfoStore> pathInfoStore;
    private final ScmConfiguration scmConfiguration;

    private LegacySupplier(Provider<ScmPathInfoStore> pathInfoStore, ScmConfiguration scmConfiguration) {
      this.pathInfoStore = pathInfoStore;
      this.scmConfiguration = scmConfiguration;
    }

    @Override
    public String get() {
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
