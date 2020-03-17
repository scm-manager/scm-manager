/**
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
package sonia.scm.schedule;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CronThreadFactoryTest {

  private Runnable doNothind = () -> {};

  @Test
  void shouldCreateThreadWithName() {
    try (CronThreadFactory threadFactory = new CronThreadFactory()) {
      Thread thread = threadFactory.newThread(doNothind);
      assertThat(thread.getName()).startsWith("CronScheduler-");
    }
  }

  @Test
  void shouldCreateThreadsWithDifferentNames() {
    try (CronThreadFactory threadFactory = new CronThreadFactory()) {
      Thread one = threadFactory.newThread(doNothind);
      Thread two = threadFactory.newThread(doNothind);
      assertThat(one.getName()).isNotEqualTo(two.getName());
    }
  }

  @Test
  void shouldCreateThreadsWithDifferentNamesFromDifferentFactories() {
    String one;
    try (CronThreadFactory threadFactory = new CronThreadFactory()) {
      one = threadFactory.newThread(doNothind).getName();
    }

    String two;
    try (CronThreadFactory threadFactory = new CronThreadFactory()) {
      two = threadFactory.newThread(doNothind).getName();
    }

    assertThat(one).isNotEqualTo(two);
  }

  @Nested
  class ShiroTests {

    @Mock
    private Subject subject;

    @BeforeEach
    void setUpContext() {
      ThreadContext.bind(subject);
    }

    @Test
    void shouldNotInheritShiroContext() throws InterruptedException {
      ShiroResourceCapturingRunnable runnable = new ShiroResourceCapturingRunnable();
      try (CronThreadFactory threadFactory = new CronThreadFactory()) {
        Thread thread = threadFactory.newThread(runnable);
        thread.start();
        thread.join();
      }
      assertThat(runnable.resources).isSameAs(Collections.emptyMap());
    }
  }


  private static class ShiroResourceCapturingRunnable implements Runnable {

    private Map<Object, Object> resources;

    @Override
    public void run() {
      resources = ThreadContext.getResources();
    }
  }
}
