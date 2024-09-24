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

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import sonia.scm.sse.Channel;
import sonia.scm.sse.ChannelRegistry;
import sonia.scm.sse.Message;

public class DefaultNotificationSender implements NotificationSender {

  @VisibleForTesting
  static final String MESSAGE_NAME = "notification";

  private final NotificationStore store;
  private final ChannelRegistry channelRegistry;

  @Inject
  public DefaultNotificationSender(NotificationStore store, ChannelRegistry channelRegistry) {
    this.store = store;
    this.channelRegistry = channelRegistry;
  }

  @Override
  public void send(Notification notification, String recipient) {
    store.add(notification, recipient);
    Channel channel = channelRegistry.channel(new NotificationChannelId(recipient));
    channel.broadcast(message(notification));
  }

  private Message message(Notification notification) {
    return new Message(MESSAGE_NAME, Notification.class, notification);
  }
}
