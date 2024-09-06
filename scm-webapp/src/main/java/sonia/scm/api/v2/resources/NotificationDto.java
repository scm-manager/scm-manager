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

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Data;
import sonia.scm.notifications.StoredNotification;
import sonia.scm.notifications.Type;

import java.time.Instant;
import java.util.Map;

@Data
public class NotificationDto extends HalRepresentation {

  private Instant createdAt;
  private Type type;
  private String link;
  private String message;
  private Map<String, String> parameters;

  public NotificationDto(StoredNotification notification, Links links) {
    super(links);
    this.type = notification.getType();
    this.link = notification.getLink();
    this.message = notification.getMessage();
    this.parameters = notification.getParameters();
    this.createdAt = notification.getCreatedAt();
  }
}
