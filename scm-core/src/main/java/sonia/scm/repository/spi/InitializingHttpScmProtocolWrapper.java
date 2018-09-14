package sonia.scm.repository.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public abstract class InitializingHttpScmProtocolWrapper implements ScmProtocolProvider {

  private static final Logger logger = LoggerFactory.getLogger(InitializingHttpScmProtocolWrapper.class);

  private final Provider<? extends ScmProviderHttpServlet> delegateProvider;
  private final Provider<ScmPathInfoStore> uriInfoStore;
  private final ScmConfiguration scmConfiguration;

  private volatile boolean isInitialized = false;


  protected InitializingHttpScmProtocolWrapper(Provider<? extends ScmProviderHttpServlet> delegateProvider, Provider<ScmPathInfoStore> uriInfoStore, ScmConfiguration scmConfiguration) {
    this.delegateProvider = delegateProvider;
    this.uriInfoStore = uriInfoStore;
    this.scmConfiguration = scmConfiguration;
  }

  protected void initializeServlet(ServletConfig config, ScmProviderHttpServlet httpServlet) throws ServletException {
    httpServlet.init(config);
  }

  @Override
  public HttpScmProtocol get(Repository repository) {
    if (!repository.getType().equals(getType())) {
      throw new IllegalArgumentException("cannot handle repository with type " + repository.getType() + " with protocol for type " + getType());
    }
    return new ProtocolWrapper(repository, computeBasePath());
  }

  private String computeBasePath() {
    return getPathFromScmPathInfoIfAvailable().orElse(getPathFromConfiguration());
  }

  private Optional<String> getPathFromScmPathInfoIfAvailable() {
    try {
      ScmPathInfoStore scmPathInfoStore = uriInfoStore.get();
      if (scmPathInfoStore != null && scmPathInfoStore.get() != null) {
        return of(scmPathInfoStore.get().getRootUri().toASCIIString());
      }
    } catch (Exception e) {
      logger.debug("could not get ScmPathInfoStore from context", e);
    }
    return empty();
  }

  private String getPathFromConfiguration() {
    logger.debug("using base path from configuration: " + scmConfiguration.getBaseUrl());
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
