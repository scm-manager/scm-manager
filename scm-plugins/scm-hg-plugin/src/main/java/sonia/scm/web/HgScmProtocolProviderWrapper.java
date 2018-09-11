package sonia.scm.web;

import sonia.scm.api.v2.resources.UriInfoStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class HgScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {
  @Inject
  public HgScmProtocolProviderWrapper(Provider<HgCGIServlet> servletProvider, Provider<HgPermissionFilter> permissionFilter, Provider<UriInfoStore> uriInfoStore, ScmConfiguration scmConfiguration) {
    super(servletProvider, permissionFilter, uriInfoStore, scmConfiguration);
  }
}
