package sonia.scm.web;

import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class GitScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {
  @Inject
  public GitScmProtocolProviderWrapper(Provider<ScmGitServlet> servletProvider, Provider<GitPermissionFilter> permissionFilter, Provider<ScmPathInfoStore> uriInfoStore, ScmConfiguration scmConfiguration) {
    super(servletProvider, permissionFilter, uriInfoStore, scmConfiguration);
  }
}
