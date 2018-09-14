package sonia.scm.web;

import com.google.inject.Inject;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletFactory;
import sonia.scm.util.Decorators;

import javax.inject.Provider;
import java.util.Set;

public class ScmGitServletProvider implements Provider<ScmProviderHttpServlet> {

  @Inject
  private Provider<ScmGitServlet> scmGitServlet;
  @Inject(optional = true)
  private Set<ScmProviderHttpServletFactory> decoratorFactories;

  @Override
  public ScmProviderHttpServlet get() {
    return Decorators.decorate(scmGitServlet.get(), decoratorFactories);
  }
}
