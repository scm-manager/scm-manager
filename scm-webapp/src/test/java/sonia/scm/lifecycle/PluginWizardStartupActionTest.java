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

package sonia.scm.lifecycle;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.WebappConfigProvider;
import sonia.scm.plugin.PluginSetConfigStore;
import sonia.scm.plugin.PluginSetsConfig;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PluginWizardStartupActionTest {

  @Mock
  private PluginSetConfigStore pluginSetConfigStore;

  @InjectMocks
  private PluginWizardStartupAction startupAction;

  @Test
  void shouldNotBeDoneByDefault() {
    WebappConfigProvider.setConfigBindings(Collections.emptyMap());
    Assertions.assertThat(startupAction.done()).isFalse();
  }

  @Test
  void shouldBeDoneIfInitialPasswordIsSet() {
    WebappConfigProvider.setConfigBindings(Map.of("initialPassword", "foo/bar"));
    Assertions.assertThat(startupAction.done()).isTrue();
  }

  @Test
  void shouldBeDoneIfConfigIsAlreadySet() {
    Mockito.when(pluginSetConfigStore.getPluginSets()).thenReturn(Optional.of(new PluginSetsConfig()));

    Assertions.assertThat(startupAction.done()).isTrue();
  }
}
