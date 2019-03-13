package sonia.scm.repository;

import javax.inject.Inject;
import java.util.Set;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

public class NamespaceStrategyValidator {

  private final Set<NamespaceStrategy> strategies;

  @Inject
  public NamespaceStrategyValidator(Set<NamespaceStrategy> strategies) {
    this.strategies = strategies;
  }

  public void check(String name) {
    doThrow()
      .violation("unknown NamespaceStrategy " + name, "namespaceStrategy")
      .when(!isValid(name));
  }

  private boolean isValid(String name) {
    return strategies.stream().anyMatch(ns -> ns.getClass().getSimpleName().equals(name));
  }
}
