package sonia.scm.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Set;

public class NamespaceStrategyProvider implements Provider<NamespaceStrategy> {

  private static final Logger LOG = LoggerFactory.getLogger(NamespaceStrategyProvider.class);

  private final Set<NamespaceStrategy> strategies;
  private final ScmConfiguration scmConfiguration;

  @Inject
  public NamespaceStrategyProvider(Set<NamespaceStrategy> strategies, ScmConfiguration scmConfiguration) {
    this.strategies = strategies;
    this.scmConfiguration = scmConfiguration;
  }

  @Override
  public NamespaceStrategy get() {
    String namespaceStrategy = scmConfiguration.getNamespaceStrategy();

    for (NamespaceStrategy s : this.strategies) {
      if (s.getClass().getSimpleName().equals(namespaceStrategy)) {
          return s;
      }
    }

    LOG.warn("could not find namespace strategy {}, using default strategy", namespaceStrategy);
    return new UsernameNamespaceStrategy();
  }

}
