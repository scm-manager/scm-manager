package sonia.scm.web;

import sonia.scm.repository.spi.InitializingHttpScmProtocolWrapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class GitScmProtocolProviderWrapper extends InitializingHttpScmProtocolWrapper {
  @Inject
  public GitScmProtocolProviderWrapper(Provider<ScmGitServlet> servletProvider) {
    super(servletProvider);
  }
}
