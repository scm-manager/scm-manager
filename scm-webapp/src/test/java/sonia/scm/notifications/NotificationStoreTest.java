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
import sonia.scm.store.InMemoryByteDataStoreFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ShiroExtension.class)
class NotificationStoreTest {

  private InMemoryByteDataStoreFactory dataStoreFactory;

  private NotificationStore store;

  private AtomicInteger counter;

  @BeforeEach
  void setUp() {
    counter = new AtomicInteger();
    dataStoreFactory = new InMemoryByteDataStoreFactory();
    store = new NotificationStore(dataStoreFactory);
  }

  @Test
  @SubjectAware("trillian")
  void shouldAddNotification() {
    Notification notification = notification();

    store.add(notification, "trillian");

    assertThat(store.getAll()).containsOnly(notification);
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

    assertThat(store.getAll()).containsOnly(one, three);
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

  private Notification notification() {
    return new Notification(Notification.Type.INFO, "Hello " + counter.incrementAndGet());
  }

}
