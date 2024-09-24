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

package sonia.scm.autoconfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.Platform;
import sonia.scm.repository.HgVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoConfiguratorProviderTest {

  @Mock
  private HgVerifier verifier;

  @Mock
  private Platform platform;

  @InjectMocks
  private AutoConfiguratorProvider provider;

  @Test
  void shouldReturnPosixAutoConfiguration() {
    when(platform.isPosix()).thenReturn(true);

    assertThat(provider.get()).isInstanceOf(PosixAutoConfigurator.class);
  }

  @Test
  void shouldReturnWindowsAutoConfiguration() {
    when(platform.isWindows()).thenReturn(true);

    assertThat(provider.get()).isInstanceOf(WindowsAutoConfigurator.class);
  }

  @Test
  void shouldReturnNoOpAutoConfiguration() {
    assertThat(provider.get()).isInstanceOf(NoOpAutoConfigurator.class);
  }
}
