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

import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.SseEventSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.SessionId;

import java.io.Closeable;
import java.time.Instant;
import java.util.function.Function;

class Client implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(Client.class);

  private final SessionId sessionId;
  private final SseEventAdapter adapter;
  private final SseEventSink eventSink;

  private Instant lastUsed;
  private boolean exceptionallyClosed = false;

  Client(Registration registration) {
    this(registration, reg -> new SseEventAdapter(reg.getSse()));
  }

  Client(Registration registration, Function<Registration, SseEventAdapter> adapterFactory) {
    sessionId = registration.getSessionId();
    adapter = adapterFactory.apply(registration);
    eventSink = registration.getEventSink();
    lastUsed = Instant.now();
  }

  Instant getLastUsed() {
    return lastUsed;
  }

  SessionId getSessionId() {
    return sessionId;
  }

  boolean isExceptionallyClosed() {
    return exceptionallyClosed;
  }

  void send(Message message) {
    if (!isClosed()) {
      OutboundSseEvent event = adapter.create(message);
      LOG.debug("send message to client with session id {}", sessionId);
      lastUsed = Instant.now();
      eventSink.send(event).exceptionally(e -> {
        if (LOG.isTraceEnabled()) {
          LOG.trace("failed to send event to client with session id {}:", sessionId, e);
        } else {
          LOG.debug("failed to send event to client with session id {}: {}", sessionId, e.getMessage());
        }
        exceptionallyClosed = true;
        close();
        return null;
      });
    } else {
      LOG.debug("client has closed the connection, before we could send the message");
    }
  }

  boolean isClosed() {
    return exceptionallyClosed || eventSink.isClosed();
  }

  @Override
  public void close() {
    LOG.trace("close sse session of client with session id: {}", sessionId);
    eventSink.close();
  }
}
