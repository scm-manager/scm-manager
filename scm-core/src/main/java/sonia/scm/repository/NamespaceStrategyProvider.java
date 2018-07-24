package sonia.scm.repository;

import sonia.scm.config.ScmConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

public class NamespaceStrategyProvider implements Provider<NamespaceStrategy> {

  private final Set<NamespaceStrategy> strategies;
  private final ScmConfiguration scmConfiguration;

  @Inject
  public NamespaceStrategyProvider(Set<NamespaceStrategy> strategies, ScmConfiguration scmConfiguration) {
    this.strategies = strategies;
    this.scmConfiguration = scmConfiguration;
  }

  @Override
  public NamespaceStrategy get() {
    String namespaceStrategy = scmConfiguration.getDefaultNamespaceStrategy();

    for (NamespaceStrategy s : this.strategies) {
      if (s.getClass().getCanonicalName().equals(namespaceStrategy)) {
          return s;
      }
    }
    return null;
  }

}
