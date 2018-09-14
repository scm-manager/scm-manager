package sonia.scm.web;

import com.google.inject.Inject;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletProvider;

import javax.inject.Provider;

public class HgCGIServletProvider extends ScmProviderHttpServletProvider {

  @Inject
  private Provider<HgCGIServlet> servletProvider;

  public HgCGIServletProvider() {
    super("hg");
  }

  @Override
  protected ScmProviderHttpServlet getRootServlet() {
    return servletProvider.get();
  }
}
