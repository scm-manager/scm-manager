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

import lombok.Value;

import java.time.Instant;
import java.util.Map;

/**
 * Notifications can be used to send a message to specific user.
 *
 * @since 2.18.0
 */
@Value
public class Notification {

  Type type;
  String link;
  String message;
  Map<String, String> parameters;
  Instant createdAt;

  public Notification(Type type, String link, String message) {
    this(type, link, message, null);
  }

  public Notification(Type type, String link, String message, Map<String, String> parameters) {
    this(type, link, message, parameters, Instant.now());
  }

  Notification(Type type, String link, String message, Map<String, String> parameters, Instant createdAt) {
    this.type = type;
    this.link = link;
    this.message = message;
    this.parameters = parameters;
    this.createdAt = createdAt;
  }

}
