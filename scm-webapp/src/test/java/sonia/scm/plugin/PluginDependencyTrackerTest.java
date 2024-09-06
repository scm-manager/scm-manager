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
