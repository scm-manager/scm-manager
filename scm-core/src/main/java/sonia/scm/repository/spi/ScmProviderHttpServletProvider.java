package sonia.scm.repository.spi;

import com.google.inject.Inject;
import sonia.scm.util.Decorators;

import javax.inject.Provider;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public abstract class ScmProviderHttpServletProvider implements Provider<ScmProviderHttpServlet> {

  @Inject(optional = true)
  private Set<ScmProviderHttpServletDecoratorFactory> decoratorFactories;

  private final String type;

  protected ScmProviderHttpServletProvider(String type) {
    this.type = type;
  }

  @Override
  public ScmProviderHttpServlet get() {
    return Decorators.decorate(getRootServlet(), decoratorFactories.stream().filter(d -> d.handlesScmType(type)).collect(toList()));
  }

  protected abstract ScmProviderHttpServlet getRootServlet();
}
