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

import org.junit.jupiter.api.Test;
import sonia.scm.config.ScmConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NamespaceStrategyProviderTest {

  @Test
  void shouldReturnConfiguredStrategy() {
    Set<NamespaceStrategy> strategies = allStrategiesAsSet();

    ScmConfiguration configuration = new ScmConfiguration();
    configuration.setNamespaceStrategy("Arthur");

    NamespaceStrategyProvider provider = new NamespaceStrategyProvider(strategies, configuration);
    NamespaceStrategy strategy = provider.get();

    assertThat(strategy).isInstanceOf(Arthur.class);
  }

  @Test
  void shouldReturnUsernameStrategyForUnknown() {
    Set<NamespaceStrategy> strategies = Collections.emptySet();

    ScmConfiguration configuration = new ScmConfiguration();
    configuration.setNamespaceStrategy("Arthur");

    NamespaceStrategyProvider provider = new NamespaceStrategyProvider(strategies, configuration);
    NamespaceStrategy strategy = provider.get();

    assertThat(strategy).isInstanceOf(UsernameNamespaceStrategy.class);
  }

  private LinkedHashSet<NamespaceStrategy> allStrategiesAsSet() {
    return new LinkedHashSet<>(Arrays.asList(new Trillian(), new Zaphod(), new Arthur()));
  }

  private static class Trillian implements NamespaceStrategy{

    @Override
    public String createNamespace(Repository repository) {
      return "trillian";
    }
  }

  private static class Zaphod implements NamespaceStrategy {

    @Override
    public String createNamespace(Repository repository) {
      return "zaphod";
    }
  }

  private static class Arthur implements NamespaceStrategy {

    @Override
    public String createNamespace(Repository repository) {
      return "arthur";
    }
  }

}
