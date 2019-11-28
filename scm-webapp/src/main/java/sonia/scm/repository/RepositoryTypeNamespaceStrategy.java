package sonia.scm.repository;

import sonia.scm.plugin.Extension;

@Extension
public class RepositoryTypeNamespaceStrategy implements NamespaceStrategy {
  @Override
  public String createNamespace(Repository repository) {
    return repository.getType();
  }
}
