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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ShiroExtension.class)
class NotificationSenderTest {

  @Test
  @SubjectAware("trillian")
  void shouldUsePrincipal() {
    CapturingNotificationSender sender = new CapturingNotificationSender();

    Notification notification = new Notification(Type.INFO, "/tricia", "Hello trillian");
    sender.send(notification);

    assertThat(sender.notification).isSameAs(notification);
    assertThat(sender.recipient).isEqualTo("trillian");
  }

  private static class CapturingNotificationSender implements NotificationSender {

    private Notification notification;
    private String recipient;

    @Override
    public void send(Notification notification, String recipient) {
      this.notification = notification;
      this.recipient = recipient;
    }
  }

}
