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

import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.SessionId;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientTest {

  @Mock
  private SseEventSink eventSink;

  @Mock
  private SseEventAdapter adapter;

  @Mock
  private Sse sse;

  @Mock
  private OutboundSseEvent sseEvent;

  @Mock
  @SuppressWarnings("rawtypes")
  private CompletionStage completionStage;

  @Test
  void shouldSetInitialLastUsed() {
    Client client = client("one");

    assertThat(client.getLastUsed()).isNotNull();
  }

  @Test
  void shouldReturnSessionId() {
    Client client = client("two");

    assertThat(client.getSessionId()).isEqualTo(SessionId.valueOf("two"));
  }

  @Test
  void shouldNotSendToClosedEventSink() {
    Client client = client("three");
    when(eventSink.isClosed()).thenReturn(true);

    client.send(message(42));

    verify(eventSink, never()).send(any(OutboundSseEvent.class));
  }

  @Test
  void shouldCloseEventSink() {
    Client client = client("one");

    client.close();

    verify(eventSink).close();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldSendMessage() {
    Message message = message(21);
    when(adapter.create(message)).thenReturn(sseEvent);
    when(eventSink.send(sseEvent)).thenReturn(completionStage);

    Client client = client("one");
    client.send(message);

    verify(eventSink).send(sseEvent);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldMarkAsClosedOnException() {
    Message message = message(21);
    when(adapter.create(message)).thenReturn(sseEvent);
    when(completionStage.exceptionally(any())).then(ic -> {
      Function<Exception,?> function = ic.getArgument(0);
      function.apply(new IllegalStateException("failed"));
      return null;
    });
    when(eventSink.send(sseEvent)).thenReturn(completionStage);

    Client client = client("one");
    client.send(message);

    verify(eventSink).close();
    assertThat(client.isExceptionallyClosed()).isTrue();
  }

  private Message message(int i) {
    return new Message("count", Integer.class, i);
  }

  private Client client(String sessionId) {
    Registration registration = new Registration(SessionId.valueOf(sessionId), sse, eventSink);
    return new Client(registration, reg -> adapter);
  }

}
