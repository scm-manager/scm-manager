package sonia.scm.web;

import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class SvnScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {
  @Inject
  public SvnScmProtocolProviderWrapper(Provider<SvnDAVServlet> servletProvider) {
    super(servletProvider);
  }
}
