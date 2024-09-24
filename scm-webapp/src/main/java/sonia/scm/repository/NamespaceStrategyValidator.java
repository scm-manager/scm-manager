/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository;

import jakarta.inject.Inject;

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
