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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.SessionId;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

public class Channel {

  private static final Logger LOG = LoggerFactory.getLogger(Channel.class);

  private final List<Client> clients = new ArrayList<>();
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private final Object channelId;
  private final Function<Registration, Client> clientFactory;

  Channel(Object channelId) {
    this(channelId, Client::new);
  }

  Channel(Object channelId, Function<Registration, Client> clientFactory) {
    this.channelId = channelId;
    this.clientFactory = clientFactory;
  }

  public void register(Registration registration) {
    registration.validate();
    Client client = clientFactory.apply(registration);

    LOG.trace("registered new client {} to channel {}", client.getSessionId(), channelId);
    lock.writeLock().lock();
    try {
      clients.add(client);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void broadcast(Message message) {
    LOG.trace("broadcast message {} to clients of channel {}", message, channelId);
    lock.readLock().lock();
    try {
      clients.stream()
        .filter(isNotSender(message))
        .forEach(client -> client.send(message));
    } finally {
      lock.readLock().unlock();
    }
  }

  private Predicate<? super Client> isNotSender(Message message) {
    return client -> {
      Optional<SessionId> senderSessionId = message.getSender();
      return senderSessionId
        .map(sessionId -> !sessionId.equals(client.getSessionId()))
        .orElse(true);
    };
  }

  void removeClosedOrTimeoutClients() {
    Instant timeoutLimit = Instant.now().minus(30, ChronoUnit.SECONDS);
    lock.writeLock().lock();
    try {
      int removeCounter = 0;
      Iterator<Client> it = clients.iterator();
      while (it.hasNext()) {
        Client client = it.next();
        if (client.isClosed()) {
          LOG.trace("remove closed client with session {}", client.getSessionId());
          it.remove();
          removeCounter++;
        } else if (client.getLastUsed().isBefore(timeoutLimit)) {
          client.close();
          LOG.trace("remove client with session {}, because it has reached the timeout", client.getSessionId());
          it.remove();
          removeCounter++;
        }
      }
      LOG.trace("removed {} closed clients from channel", removeCounter);
    } finally {
      lock.writeLock().unlock();
    }
  }

}
