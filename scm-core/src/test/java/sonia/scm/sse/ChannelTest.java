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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.SessionId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
