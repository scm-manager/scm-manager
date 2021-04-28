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

package sonia.scm.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.SessionId;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;
import java.io.Closeable;
import java.time.Instant;

class Client implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(Client.class);

  private final SessionId sessionId;
  private final SseEventAdapter adapter;
  private final SseEventSink eventSink;

  private Instant lastUsed;
  private boolean exceptionallyClosed = false;

  Client(Registration registration) {
    sessionId = registration.getSessionId();
    adapter = new SseEventAdapter(registration.getSse());
    eventSink = registration.getEventSink();
  }

  Instant getLastUsed() {
    return lastUsed;
  }

  SessionId getSessionId() {
    return sessionId;
  }

  void send(Message message) {
    OutboundSseEvent event = adapter.create(message);
    if (!isClosed()) {
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
