package sonia.scm.web;

import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
@Extension
public class HgScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {
  @Inject
  public HgScmProtocolProviderWrapper(Provider<HgCGIServlet> servletProvider, Provider<HgPermissionFilter> permissionFilter, Provider<ScmPathInfoStore> uriInfoStore, ScmConfiguration scmConfiguration) {
    super(servletProvider, permissionFilter, uriInfoStore, scmConfiguration);
  }

  @Override
  public String getType() {
    return "hg";
  }
}
