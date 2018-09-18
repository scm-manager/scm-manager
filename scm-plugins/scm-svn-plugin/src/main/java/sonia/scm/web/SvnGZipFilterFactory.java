package sonia.scm.web;

import com.google.inject.Inject;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletDecoratorFactory;

@Extension
public class SvnGZipFilterFactory implements ScmProviderHttpServletDecoratorFactory {

  private final SvnRepositoryHandler handler;

  @Inject
  public SvnGZipFilterFactory(SvnRepositoryHandler handler) {
    this.handler = handler;
  }

  @Override
  public boolean handlesScmType(String type) {
    return SvnRepositoryHandler.TYPE_NAME.equals(type);
  }

  @Override
  public ScmProviderHttpServlet createDecorator(ScmProviderHttpServlet delegate) {
    return new SvnGZipFilter(handler, delegate);
  }
}
