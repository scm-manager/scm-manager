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
