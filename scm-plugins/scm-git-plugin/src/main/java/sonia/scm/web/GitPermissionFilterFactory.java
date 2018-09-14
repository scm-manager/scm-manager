package sonia.scm.web;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletDecoratorFactory;

import javax.inject.Inject;

@Extension
public class GitPermissionFilterFactory implements ScmProviderHttpServletDecoratorFactory {

  private final ScmConfiguration configuration;

  @Inject
  public GitPermissionFilterFactory(ScmConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public boolean handlesScmType(String type) {
    return "git".equals(type);
  }

  @Override
  public ScmProviderHttpServlet createDecorator(ScmProviderHttpServlet delegate) {
    return new GitPermissionFilter(configuration, delegate);
  }
}
