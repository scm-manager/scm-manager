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
