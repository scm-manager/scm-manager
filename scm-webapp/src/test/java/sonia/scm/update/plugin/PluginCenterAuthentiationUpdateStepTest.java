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

package sonia.scm.update.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.PluginCenterAuthenticator;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginCenterAuthenticationUpdateStepTest {

  private PluginCenterAuthenticationUpdateStep updateStep;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConfigurationStoreFactory configurationStoreFactory;
  @Mock
  private ConfigurationStore<PluginCenterAuthenticator.Authentication> configurationStore;

  @BeforeEach
  void initUpdateStep() {
    when(configurationStoreFactory.withType(PluginCenterAuthenticator.Authentication.class).withName("plugin-center-auth").build())
      .thenReturn(configurationStore);
    updateStep = new PluginCenterAuthenticationUpdateStep(configurationStoreFactory);
  }

  @Test
  void shouldNotUpdateIfConfigFileNotAvailable() throws Exception {
    when(configurationStore.getOptional()).thenReturn(Optional.empty());

    updateStep.doUpdate();

    verify(configurationStore, never()).set(any());
  }

  @Test
  void shouldUpdateIfRefreshTokenNotEncrypted() throws Exception {
    when(configurationStore.getOptional())
      .thenReturn(Optional.of(new PluginCenterAuthenticator.Authentication("trillian", "trillian", "some_not_encrypted_token", Instant.now(), false)));

    updateStep.doUpdate();

    verify(configurationStore).set(argThat(config -> {
      assertThat(config.getRefreshToken()).startsWith("{enc}");
      return true;
    }));
  }

  @Test
  void shouldNotUpdateIfRefreshTokenIsAlreadyEncrypted() throws Exception {
    when(configurationStore.getOptional())
      .thenReturn(Optional.of(new PluginCenterAuthenticator.Authentication("trillian", "trillian", "{enc}my_encrypted_token", Instant.now(), false)));

    updateStep.doUpdate();

    verify(configurationStore, never()).set(any());
  }
}
