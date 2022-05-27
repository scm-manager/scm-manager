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

package sonia.scm.lifecycle;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.PluginSetConfigStore;
import sonia.scm.plugin.PluginSetsConfig;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PluginWizardStartupActionTest {

  @Mock
  private PluginSetConfigStore pluginSetConfigStore;

  @InjectMocks
  private PluginWizardStartupAction startupAction;

  @BeforeEach
  void setup() {
    System.clearProperty(AdminAccountStartupAction.INITIAL_PASSWORD_PROPERTY);
  }

  @Test
  void shouldNotBeDoneByDefault() {
    Assertions.assertThat(startupAction.done()).isFalse();
  }

  @Test
  void shouldBeDoneIfInitialPasswordIsSet() {
    System.setProperty(AdminAccountStartupAction.INITIAL_PASSWORD_PROPERTY, "secret");

    Assertions.assertThat(startupAction.done()).isTrue();
  }

  @Test
  void shouldBeDoneIfConfigIsAlreadySet() {
    Mockito.when(pluginSetConfigStore.getPluginSets()).thenReturn(Optional.of(new PluginSetsConfig()));

    Assertions.assertThat(startupAction.done()).isTrue();
  }

}
