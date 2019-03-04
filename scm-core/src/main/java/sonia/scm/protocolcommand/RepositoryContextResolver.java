package sonia.scm.protocolcommand;

import sonia.scm.plugin.ExtensionPoint;

@FunctionalInterface
@ExtensionPoint
public interface RepositoryContextResolver {

  RepositoryContext resolve(String[] args);

}
