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

import lombok.Getter;
import sonia.scm.security.SessionId;

import java.util.Optional;

@Getter
public class Message {

  private final String name;
  private final Class<?> type;
  private final Object data;
  private final SessionId sender;

  public Message(String name, Class<?> type, Object data) {
    this(name, type, data, null);
  }

  public Message(String name, Class<?> type, Object data, SessionId sender) {
    this.name = name;
    this.type = type;
    this.data = data;
    this.sender = sender;
  }

  public Optional<SessionId> getSender() {
    return Optional.ofNullable(sender);
  }
}
