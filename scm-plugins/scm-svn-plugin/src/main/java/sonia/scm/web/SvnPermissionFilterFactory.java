package sonia.scm.web;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletDecoratorFactory;

import javax.inject.Inject;

@Extension
public class SvnPermissionFilterFactory implements ScmProviderHttpServletDecoratorFactory {

  private final ScmConfiguration configuration;

  @Inject
  public SvnPermissionFilterFactory(ScmConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public boolean handlesScmType(String type) {
    return SvnRepositoryHandler.TYPE_NAME.equals(type);
  }

  @Override
  public ScmProviderHttpServlet createDecorator(ScmProviderHttpServlet delegate) {
    return new SvnPermissionFilter(configuration, delegate);
  }
}
