package sonia.scm.repository;

import sonia.scm.plugin.ExtensionPoint;

/**
 * Strategy to create a namespace for the new repository. Namespaces are used to order and identify repositories.
 */
@ExtensionPoint
public interface NamespaceStrategy {

  /**
   * Create new namespace for the given repository.
   *
   * @param repository repository
   *
   * @return namespace
   */
  String createNamespace(Repository repository);
}
