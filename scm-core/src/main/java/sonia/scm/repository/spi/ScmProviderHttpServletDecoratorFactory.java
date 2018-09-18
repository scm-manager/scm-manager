package sonia.scm.repository.spi;

import sonia.scm.DecoratorFactory;
import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface ScmProviderHttpServletDecoratorFactory extends DecoratorFactory<ScmProviderHttpServlet> {
  /**
   * Has to return <code>true</code> if this factory provides a decorator for the given scm type (eg. "git", "hg" or
   * "svn").
   * @param type The current scm type this factory can provide a decorator for.
   * @return <code>true</code> when the provided decorator should be used for the given scm type.
   */
  boolean handlesScmType(String type);
}
