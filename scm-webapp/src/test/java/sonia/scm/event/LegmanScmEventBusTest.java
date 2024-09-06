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

package sonia.scm.event;

import com.github.legman.Subscribe;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@ExtendWith(MockitoExtension.class)
class LegmanScmEventBusTest {

  private LegmanScmEventBus eventBus;

  @BeforeEach
  void setUp() {
    DefaultSecurityManager manager = new DefaultSecurityManager();
    ThreadContext.bind(manager);
    // we have to use a single thread executor to ensure that the
    // same thread is used for the test. This allows us to test,
    // that even if the same thread is used the correct subject is in place.
    eventBus = new LegmanScmEventBus(Executors.newSingleThreadExecutor());
  }

  @AfterEach
  void tearDown() {
    eventBus.shutdownEventBus(new ShutdownEventBusEvent());
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
  }

  @Test
  void shouldPassShiroContext() {
    bindSubject("dent");

    PrincipalCapturingSubscriber subscriber = new PrincipalCapturingSubscriber();
    eventBus.register(subscriber);
    eventBus.post("first");

    bindSubject("trillian");
    eventBus.post("second");

    await()
      .atMost(1, TimeUnit.SECONDS)
      .until(() -> "trillian".equals(subscriber.principal));
  }

  private void bindSubject(String principal) {
    Subject dent = new Subject.Builder()
      .authenticated(true)
      .principals(new SimplePrincipalCollection(principal, "test"))
      .buildSubject();
    ThreadContext.bind(dent);
  }

  static class PrincipalCapturingSubscriber {

    private Object principal;

    @Subscribe
    public void handle(String event) {
      principal = SecurityUtils.getSubject().getPrincipal();
    }

  }

}
