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

import com.google.common.base.Preconditions;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import lombok.Value;
import sonia.scm.security.SessionId;

@Value
public class Registration {

  SessionId sessionId;
  Sse sse;
  SseEventSink eventSink;

  void validate() {
    Preconditions.checkNotNull(sessionId, "sessionId is required");
    Preconditions.checkNotNull(sse, "sse is required");
    Preconditions.checkNotNull(eventSink, "eventSink is required");
  }
}
