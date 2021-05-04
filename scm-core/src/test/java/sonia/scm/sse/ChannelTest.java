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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.SessionId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelTest {

  private Map<Registration,Client> clients;
  private Channel channel;

  @BeforeEach
  void setUp()  {
    this.clients = new HashMap<>();
    channel = new Channel("one", registration -> clients.get(registration));
  }

  @Test
  void shouldRegisterAndSend() {
    Client client = register();

    Message message = broadcast("Hello World");

    verify(client).send(message);
  }

  @Test
  void shouldNotSendToSender() {
    Client clientOne = register();
    SessionId sessionOne = SessionId.valueOf("one");
    when(clientOne.getSessionId()).thenReturn(sessionOne);

    Client clientTwo = register();
    when(clientTwo.getSessionId()).thenReturn(SessionId.valueOf("two"));

    Message message = broadcast("Hello Two", sessionOne);

    verify(clientOne, never()).send(message);
    verify(clientTwo).send(message);
  }

  @Test
  void shouldRemoveClosedClients() {
    Client closedClient = register();
    when(closedClient.isClosed()).thenReturn(true);
    Client activeClient = register();
    when(activeClient.getLastUsed()).thenReturn(Instant.now());

    channel.removeClosedOrTimeoutClients();

    Message message = broadcast("Hello active ones");

    verify(closedClient, never()).send(message);
    verify(activeClient).send(message);
  }

  @Test
  void shouldRemoveClientsWhichAreNotUsedWithin30Seconds() {
    Client timedOutClient = register();
    when(timedOutClient.getLastUsed()).thenReturn(Instant.now().minus(31L, ChronoUnit.SECONDS));
    Client activeClient = register();
    when(activeClient.getLastUsed()).thenReturn(Instant.now().minus(29L, ChronoUnit.SECONDS));

    channel.removeClosedOrTimeoutClients();

    Message message = broadcast("Hello active ones");
    verify(timedOutClient, never()).send(message);
    verify(timedOutClient).close();
    verify(activeClient).send(message);
    verify(activeClient, never()).close();
  }

  private Client register() {
    Registration registration = mock(Registration.class);
    Client client = mock(Client.class);
    clients.put(registration, client);
    channel.register(registration);
    return client;
  }

  private Message broadcast(String data) {
    return broadcast(data, null);
  }

  private Message broadcast(String data, SessionId sessionId) {
    Message message = new Message("hello", String.class, data, sessionId);
    channel.broadcast(message);
    return message;
  }

}
