package sonia.scm.web;

import com.google.inject.Inject;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletProvider;

import javax.inject.Provider;

public class HgCGIServletProvider extends ScmProviderHttpServletProvider {

  @Inject
  private Provider<HgCGIServlet> servletProvider;

  public HgCGIServletProvider() {
    super(HgRepositoryHandler.TYPE_NAME);
  }

  @Override
  protected ScmProviderHttpServlet getRootServlet() {
    return servletProvider.get();
  }
}
