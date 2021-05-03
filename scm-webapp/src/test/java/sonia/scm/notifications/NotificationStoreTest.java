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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.security.KeyGenerator;
import sonia.scm.security.UUIDKeyGenerator;
import sonia.scm.store.InMemoryByteDataStoreFactory;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ShiroExtension.class)
class NotificationStoreTest {

  private NotificationStore store;
  private final KeyGenerator keyGenerator = new UUIDKeyGenerator();

  private AtomicInteger counter;

  @BeforeEach
  void setUp() {
    counter = new AtomicInteger();
    store = new NotificationStore(new InMemoryByteDataStoreFactory(), keyGenerator);
  }

  @Test
  @SubjectAware("trillian")
  void shouldAddNotification() {
    Notification notification = notification();

    store.add(notification, "trillian");

    containsMessage(notification);
  }

  @Test
  @SubjectAware("trillian")
  void shouldReturnId() {
    Notification notification = notification();

    String id = store.add(notification, "trillian");
    assertThat(id).isNotNull().isNotEmpty();
  }

  @Test
  @SubjectAware("trillian")
  void shouldAssignId() {
    Notification notification = notification();

    store.add(notification, "trillian");

    StoredNotification storedNotification = store.getAll().get(0);
    assertThat(storedNotification.getId()).isNotNull().isNotEmpty();
  }

  @Test
  @SubjectAware("trillian")
  void shouldCopyProperties() {
    Notification notification = notification();

    store.add(notification, "trillian");

    StoredNotification storedNotification = store.getAll().get(0);
    assertThat(storedNotification)
      .usingRecursiveComparison()
      .ignoringFields("id")
      .isEqualTo(notification);
  }

  private void containsMessage(Notification... notifications) {
    String[] messages = Arrays.stream(notifications).map(Notification::getMessage).toArray(String[]::new);
    Stream<String> storedMessages = store.getAll().stream().map(StoredNotification::getMessage);
    assertThat(storedMessages).containsOnly(messages);
  }

  @Test
  @SubjectAware("trillian")
  void shouldOnlyReturnNotificationsForPrincipal() {
    Notification one = notification();
    Notification two = notification();
    Notification three = notification();

    store.add(one, "trillian");
    store.add(two, "dent");
    store.add(three, "trillian");

    containsMessage(one, three);
  }

  @Test
  @SubjectAware("slarti")
  void shouldClearOnlyPrincipalNotifications() {
    Notification one = notification();
    Notification two = notification();
    Notification three = notification();

    store.add(one, "slarti");
    store.add(two, "slarti");
    store.add(three, "slarti");

    store.clear();
    assertThat(store.getAll()).isEmpty();
  }

  @Test
  @SubjectAware("slarti")
  void shouldRemoveNotificationWithId() {
    Notification one = notification();
    Notification two = notification();

    String id = store.add(one, "slarti");
    store.add(two, "slarti");

    store.remove(id);

    containsMessage(two);
  }

  private Notification notification() {
    return new Notification(Type.INFO, "/greeting", "Hello " + counter.incrementAndGet());
  }

}
