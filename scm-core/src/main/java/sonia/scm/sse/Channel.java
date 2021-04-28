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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class Channel {

  private static final Logger LOG = LoggerFactory.getLogger(Channel.class);

  private final List<Client> clients = new ArrayList<>();
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private final Object channelId;

  Channel(Object channelId) {
    this.channelId = channelId;
  }

  public void register(Registration registration) {
    registration.validate();
    Client client = new Client(registration);

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
