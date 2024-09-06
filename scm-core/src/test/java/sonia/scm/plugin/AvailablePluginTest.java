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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AvailablePluginTest {

  @Mock
  private AvailablePluginDescriptor descriptor;

  @Test
  void shouldReturnNewPendingPluginOnInstall() {
    AvailablePlugin plugin = new AvailablePlugin(descriptor);
    assertThat(plugin.isPending()).isFalse();

    AvailablePlugin installed = plugin.install();
    assertThat(installed.isPending()).isTrue();
  }

  @Test
  void shouldThrowIllegalStateExceptionIfAlreadyPending() {
    AvailablePlugin plugin = new AvailablePlugin(descriptor).install();
    assertThrows(IllegalStateException.class, () -> plugin.install());
  }

}
