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

import com.google.common.util.concurrent.Striped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.shiro.SecurityUtils;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.xml.XmlInstantAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Singleton
@SuppressWarnings("UnstableApiUsage") // striped is still marked as beta
public class NotificationStore {

  private static final String NAME = "notifications";
  private final DataStore<Notifications> store;
  private final Striped<ReadWriteLock> locks = Striped.readWriteLock(10);

  @Inject
  public NotificationStore(DataStoreFactory dataStoreFactory) {
    this.store = dataStoreFactory.withType(Notifications.class)
      .withName(NAME)
      .build();
  }

  public void add(Notification notification, String username) {
    Lock lock = locks.get(username).writeLock();
    try {
      lock.lock();
      Notifications notifications = get(username);
      notifications.getEntries().add(notification);
      store.put(username, notifications);
    } finally {
      lock.unlock();
    }
  }

  int size(String username) {
    Lock lock = locks.get(username).readLock();
    try {
      return get(username).getEntries().size();
    } finally {
      lock.unlock();
    }
  }

  private Notifications get(String username) {
    return store.getOptional(username).orElse(new Notifications());
  }

  public List<Notification> getAll() {
    String username = getCurrentUsername();
    Lock lock = locks.get(username).readLock();
    try {
      lock.lock();
      Notifications notifications = get(username);
      return Collections.unmodifiableList(notifications.getEntries());
    } finally {
      lock.unlock();
    }
  }

  public void clear() {
    String username = getCurrentUsername();
    Lock lock = locks.get(username).writeLock();
    try {
      lock.lock();
      Notifications notifications = get(username);
      notifications.getEntries().clear();
      store.put(username, notifications);
    } finally {
      lock.unlock();
    }
  }

  private String getCurrentUsername() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  @Data
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  static class Notifications {
    @XmlElement(name = "notification")
    @XmlJavaTypeAdapter(NotificationAdapter.class)
    @XmlElementWrapper(name = "notifications")
    private List<Notification> entries;

    public List<Notification> getEntries() {
      if (entries == null) {
        entries = new ArrayList<>();
      }
      return entries;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @XmlAccessorType(XmlAccessType.FIELD)
  static class StoredNotification {
    @XmlJavaTypeAdapter(XmlInstantAdapter.class)
    Instant instant;
    Notification.Type type;
    String message;
  }

  static class NotificationAdapter extends XmlAdapter<StoredNotification, Notification> {

    @Override
    public StoredNotification marshal(Notification notification) {
      return new StoredNotification(
        notification.getCreatedAt(),
        notification.getType(),
        notification.getMessage()
      );
    }

    @Override
    public Notification unmarshal(StoredNotification storedNotification) throws Exception {
      return new Notification(
        storedNotification.getInstant(),
        storedNotification.getType(),
        storedNotification.getMessage()
      );
    }
  }

}
