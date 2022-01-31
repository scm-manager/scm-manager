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
