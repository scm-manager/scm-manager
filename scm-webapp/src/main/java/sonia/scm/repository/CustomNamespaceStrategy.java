package sonia.scm.repository;

import sonia.scm.plugin.Extension;
import sonia.scm.util.ValidationUtil;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Extension
public class CustomNamespaceStrategy implements NamespaceStrategy {
  @Override
  public String createNamespace(Repository repository) {
    String namespace = repository.getNamespace();

    doThrow()
      .violation("invalid namespace", "namespace")
      .when(!ValidationUtil.isRepositoryNameValid(namespace));

    return namespace;
  }
}
