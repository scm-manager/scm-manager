package sonia.scm.web;

import com.google.inject.Inject;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletDecoratorFactory;
import sonia.scm.util.Decorators;

import javax.inject.Provider;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public abstract class ScmProviderHttpServletProvider implements Provider<ScmProviderHttpServlet> {

  @Inject(optional = true)
  private Set<ScmProviderHttpServletDecoratorFactory> decoratorFactories;

  @Override
  public ScmProviderHttpServlet get() {
    return Decorators.decorate(getRootServlet(), decoratorFactories.stream().filter(d -> d.handlesScmType("git")).collect(toList()));
  }

  protected abstract ScmProviderHttpServlet getRootServlet();
}
