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

package sonia.scm.notifications;

import com.github.legman.Subscribe;
import com.google.common.util.concurrent.Striped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import sonia.scm.HandlerEventType;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.user.UserEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

@Singleton
@SuppressWarnings("UnstableApiUsage") // striped is still marked as beta
public class NotificationStore {

  private static final String NAME = "notifications";
  private final DataStore<StoredNotifications> store;
  private final KeyGenerator keyGenerator;
  private final Striped<ReadWriteLock> locks = Striped.readWriteLock(10);

  @Inject
  public NotificationStore(DataStoreFactory dataStoreFactory, KeyGenerator keyGenerator) {
    this.store = dataStoreFactory.withType(StoredNotifications.class)
      .withName(NAME)
      .build();
    this.keyGenerator = keyGenerator;
  }

  public String add(Notification notification, String username) {
    Lock lock = locks.get(username).writeLock();
    try {
      lock.lock();
      StoredNotifications notifications = get(username);
      String id = keyGenerator.createKey();
      notifications.getEntries().add(new StoredNotification(id, notification));
      store.put(username, notifications);
      return id;
    } finally {
      lock.unlock();
    }
  }

  private StoredNotifications get(String username) {
    return store.getOptional(username).orElse(new StoredNotifications());
  }

  public List<StoredNotification> getAll() {
    String username = getCurrentUsername();
    Lock lock = locks.get(username).readLock();
    try {
      lock.lock();
      StoredNotifications notifications = get(username);
      return Collections.unmodifiableList(notifications.getEntries());
    } finally {
      lock.unlock();
    }
  }

  public void remove(String id) {
    String username = getCurrentUsername();
    Lock lock = locks.get(username).writeLock();
    try {
      lock.lock();
      StoredNotifications notifications = get(username);
      List<StoredNotification> entries = notifications.getEntries()
        .stream()
        .filter(n -> !id.equals(n.id))
        .collect(Collectors.toList());
      notifications.setEntries(entries);
      store.put(username, notifications);
    } finally {
      lock.unlock();
    }
  }

  public void clear() {
    String username = getCurrentUsername();
    Lock lock = locks.get(username).writeLock();
    try {
      lock.lock();
      StoredNotifications notifications = get(username);
      notifications.getEntries().clear();
      store.put(username, notifications);
    } finally {
      lock.unlock();
    }
  }

  private String getCurrentUsername() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  @Subscribe
  public void handle(UserEvent event) {
    if (event.getEventType() == HandlerEventType.DELETE) {
      String username = event.getItem().getName();
      Lock lock = locks.get(username).writeLock();
      try {
        lock.lock();
        store.remove(username);
      } finally {
        lock.unlock();
      }
    }
  }

  @Data
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  static class StoredNotifications {
    @XmlElement(name = "notification")
    @XmlElementWrapper(name = "notifications")
    private List<StoredNotification> entries;

    public List<StoredNotification> getEntries() {
      if (entries == null) {
        entries = new ArrayList<>();
      }
      return entries;
    }
  }

}
