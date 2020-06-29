/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

  private static class Trillian implements NamespaceStrategy {

    @Override
    public String createNamespace(Repository repository) {
      return "trillian";
    }

    @Override
    public boolean canBeChanged() {
      return false;
    }
  }

  private static class Zaphod implements NamespaceStrategy {

    @Override
    public String createNamespace(Repository repository) {
      return "zaphod";
    }

    @Override
    public boolean canBeChanged() {
      return false;
    }
  }

  private static class Arthur implements NamespaceStrategy {

    @Override
    public String createNamespace(Repository repository) {
      return "arthur";
    }

    @Override
    public boolean canBeChanged() {
      return false;
    }
  }

}
