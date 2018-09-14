package sonia.scm.repository.spi;

import sonia.scm.DecoratorFactory;
import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface ScmProviderHttpServletFactory extends DecoratorFactory<ScmProviderHttpServlet> {}
