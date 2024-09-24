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

package sonia.scm.notifications;

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import sonia.scm.HandlerEventType;
import sonia.scm.security.KeyGenerator;
import sonia.scm.security.UUIDKeyGenerator;
import sonia.scm.store.InMemoryByteDataStoreFactory;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserTestData;

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

  @Test
  @SubjectAware("slarti")
  void shouldRemoveEntryIfUserIsDeleted() {
    store.add(notification(), "slarti");
    store.handle(new UserEvent(HandlerEventType.DELETE, UserTestData.createSlarti()));

    assertThat(store.getAll()).isEmpty();
  }

  @SubjectAware("slarti")
  @ParameterizedTest(name = "shouldIgnoreEvent_{argumentsWithNames}")
  @EnumSource(value = HandlerEventType.class, mode = EnumSource.Mode.EXCLUDE, names = "DELETE")
  void shouldIgnoreNonDeleteEvents(HandlerEventType event) {
    store.add(notification(), "slarti");
    store.handle(new UserEvent(event, UserTestData.createSlarti()));

    assertThat(store.getAll()).hasSize(1);
  }

  private Notification notification() {
    return new Notification(Type.INFO, "/greeting", "Hello " + counter.incrementAndGet());
  }

}
