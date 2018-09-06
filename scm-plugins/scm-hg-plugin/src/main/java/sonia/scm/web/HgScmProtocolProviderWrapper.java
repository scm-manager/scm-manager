package sonia.scm.web;

import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class HgScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {
  @Inject
  public HgScmProtocolProviderWrapper(Provider<HgCGIServlet> servletProvider) {
    super(servletProvider);
  }
}
