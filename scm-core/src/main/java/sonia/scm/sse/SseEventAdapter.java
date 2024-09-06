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

package sonia.scm.sse;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;

class SseEventAdapter {

  private final Sse sse;

  SseEventAdapter(Sse sse) {
    this.sse = sse;
  }

  OutboundSseEvent create(Message message) {
    return sse.newEventBuilder()
      .name(message.getName())
      .mediaType(MediaType.APPLICATION_JSON_TYPE)
      .data(message.getType(), message.getData())
      .build();
  }

}
