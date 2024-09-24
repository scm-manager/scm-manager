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
