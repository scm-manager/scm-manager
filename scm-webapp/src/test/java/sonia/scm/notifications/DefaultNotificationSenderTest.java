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

package sonia.scm.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.sse.Channel;
import sonia.scm.sse.ChannelRegistry;
import sonia.scm.sse.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultNotificationSenderTest {

  @Mock
  private NotificationStore store;

  @Mock
  private ChannelRegistry channelRegistry;

  @InjectMocks
  private DefaultNotificationSender sender;

  @Mock
  private Channel channel;

  @Test
  void shouldDelegateToStore() {
    when(channelRegistry.channel(any())).thenReturn(channel);

    Notification notification = new Notification(Type.ERROR, "/fail", "Everything has failed");

    sender.send(notification, "trillian");

    verify(store).add(notification, "trillian");
  }

  @Test
  void shouldSendToChannel() {
    NotificationChannelId channelId = new NotificationChannelId("trillian");
    when(channelRegistry.channel(channelId)).thenReturn(channel);

    Notification notification = new Notification(Type.WARNING, "/warn", "Everything looks strange");

    sender.send(notification, "trillian");
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(channel).broadcast(messageCaptor.capture());

    Message message = messageCaptor.getValue();
    assertThat(message.getName()).isEqualTo(DefaultNotificationSender.MESSAGE_NAME);
    assertThat(message.getType()).isEqualTo(Notification.class);
    assertThat(message.getData()).isEqualTo(notification);
  }
}
