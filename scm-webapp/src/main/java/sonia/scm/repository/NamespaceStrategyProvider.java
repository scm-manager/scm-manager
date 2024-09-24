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
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;

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
