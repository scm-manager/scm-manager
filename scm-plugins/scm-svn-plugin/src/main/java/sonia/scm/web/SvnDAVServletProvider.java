package sonia.scm.web;

import com.google.inject.Inject;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletProvider;

import javax.inject.Provider;

public class SvnDAVServletProvider extends ScmProviderHttpServletProvider {

  @Inject
  private Provider<SvnDAVServlet> servletProvider;

  public SvnDAVServletProvider() {
    super(SvnRepositoryHandler.TYPE_NAME);
  }

  @Override
  protected ScmProviderHttpServlet getRootServlet() {
    return servletProvider.get();
  }
}
