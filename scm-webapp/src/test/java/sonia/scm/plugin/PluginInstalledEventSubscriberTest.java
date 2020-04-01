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

package sonia.scm.plugin;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginInstalledEventSubscriberTest {

  private InstalledPlugin oldPlugin = PluginTestHelper.createInstalled("scm-hitchhiker-plugin");
  private AvailablePlugin newPlugin = PluginTestHelper.createAvailable("scm-hitchhiker-plugin", "1.1");

  @Mock
  private Subject subject;

  @Mock
  private ScmEventBus eventBus;

  @Mock
  private PluginEvent event;

  @Mock
  private PluginManager pluginManager;

  @Captor
  private ArgumentCaptor<PluginInstalledEventSubscriber.PluginInstalledEvent> eventCaptor;

  @InjectMocks
  private PluginInstalledEventSubscriber subscriber;

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldFireMyEvent() {
    when(event.getEventType()).thenReturn(PluginEventType.INSTALLED);
    when(event.getPlugin()).thenReturn(newPlugin);
    when(pluginManager.getInstalled("scm-hitchhiker-plugin")).thenReturn(Optional.of(oldPlugin));

    subscriber.handleEvent(event);

    verify(eventBus).post(eventCaptor.capture());

    PluginInstalledEventSubscriber.PluginInstalledEvent pluginInstalledEvent = eventCaptor.getValue();
    assertThat(pluginInstalledEvent.getPermission()).isEqualTo("plugin:manage");
    assertThat(pluginInstalledEvent.getPreviousPluginVersion()).isEqualTo("1.0");
    assertThat(pluginInstalledEvent.getNewPluginVersion()).isEqualTo("1.1");
    assertThat(pluginInstalledEvent.getPluginName()).isEqualTo(newPlugin.getDescriptor().getInformation().getDisplayName());
  }

  @Test
  void shouldNotFireMyEventWhenNotCreatedEvent() {
    when(event.getEventType()).thenReturn(PluginEventType.INSTALLATION_FAILED);
    subscriber.handleEvent(event);

    verify(eventBus, never()).post(eventCaptor.capture());
  }

}
