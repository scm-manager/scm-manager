/**
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
package sonia.scm.plugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sonia.scm.ScmConstraintViolationException;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginTestHelper.createInstalled;

class PluginDependencyTrackerTest {

  @Test
  void simpleInstalledPluginWithoutDependingPluginsCanBeUninstalled() {
    PluginDescriptor mail = createInstalled("scm-mail-plugin").getDescriptor();
    when(mail.getDependencies()).thenReturn(emptySet());

    PluginDependencyTracker pluginDependencyTracker = new PluginDependencyTracker();
    pluginDependencyTracker.addInstalled(mail);

    boolean mayUninstall = pluginDependencyTracker.mayUninstall("scm-mail-plugin");
    assertThat(mayUninstall).isTrue();
  }

  @Test
  void installedPluginWithDependingPluginCannotBeUninstalled() {
    PluginDescriptor review = createInstalled("scm-review-plugin").getDescriptor();
    when(review.getDependencies()).thenReturn(singleton("scm-mail-plugin"));

    PluginDependencyTracker pluginDependencyTracker = new PluginDependencyTracker();
    pluginDependencyTracker.addInstalled(review);

    boolean mayUninstall = pluginDependencyTracker.mayUninstall("scm-mail-plugin");
    assertThat(mayUninstall).isFalse();
  }

  @Test
  void uninstallOfRequiredPluginShouldThrowException() {
    PluginDescriptor review = createInstalled("scm-review-plugin").getDescriptor();
    when(review.getDependencies()).thenReturn(singleton("scm-mail-plugin"));

    PluginDependencyTracker pluginDependencyTracker = new PluginDependencyTracker();
    pluginDependencyTracker.addInstalled(review);

    Assertions.assertThrows(
      ScmConstraintViolationException.class,
      () -> pluginDependencyTracker.removeInstalled(createInstalled("scm-mail-plugin").getDescriptor())
    );
  }

  @Test
  void installedPluginWithDependingPluginCanBeUninstalledAfterDependingPluginIsUninstalled() {
    PluginDescriptor review = createInstalled("scm-review-plugin").getDescriptor();
    when(review.getDependencies()).thenReturn(singleton("scm-mail-plugin"));

    PluginDependencyTracker pluginDependencyTracker = new PluginDependencyTracker();
    pluginDependencyTracker.addInstalled(review);
    pluginDependencyTracker.removeInstalled(review);

    boolean mayUninstall = pluginDependencyTracker.mayUninstall("scm-mail-plugin");
    assertThat(mayUninstall).isTrue();
  }

  @Test
  void installedPluginWithMultipleDependingPluginCannotBeUninstalledAfterOnlyOneDependingPluginIsUninstalled() {
    PluginDescriptor review = createInstalled("scm-review-plugin").getDescriptor();
    when(review.getDependencies()).thenReturn(singleton("scm-mail-plugin"));
    PluginDescriptor jira = createInstalled("scm-jira-plugin").getDescriptor();
    when(jira.getDependencies()).thenReturn(singleton("scm-mail-plugin"));

    PluginDependencyTracker pluginDependencyTracker = new PluginDependencyTracker();
    pluginDependencyTracker.addInstalled(review);
    pluginDependencyTracker.addInstalled(jira);
    pluginDependencyTracker.removeInstalled(review);

    boolean mayUninstall = pluginDependencyTracker.mayUninstall("scm-mail-plugin");
    assertThat(mayUninstall).isFalse();
  }
}
