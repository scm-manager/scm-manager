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

import org.apache.shiro.SecurityUtils;

/**
 * Service for sending notifications.
 *
 * @since 2.18.0
 */
public interface NotificationSender {

  /**
   * Sends the notification to a specific user.
   * @param notification notification to send
   * @param recipient username of the receiving user
   */
  void send(Notification notification, String recipient);

  /**
   * Sends the notification to the current user.
   * @param notification notification to send
   */
  default void send(Notification notification) {
    send(notification, SecurityUtils.getSubject().getPrincipal().toString());
  }
}
