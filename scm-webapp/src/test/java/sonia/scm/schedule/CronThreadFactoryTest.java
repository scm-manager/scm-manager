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
