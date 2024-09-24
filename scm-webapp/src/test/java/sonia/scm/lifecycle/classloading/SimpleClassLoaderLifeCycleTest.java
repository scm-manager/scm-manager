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

package sonia.scm.lifecycle.classloading;

import org.junit.jupiter.api.Test;

import java.io.Closeable;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleClassLoaderLifeCycleTest {

  @Test
  void shouldCloseClosableClassLoaderOnShutdown() {
    SimpleClassLoaderLifeCycle lifeCycle = new SimpleClassLoaderLifeCycle(Thread.currentThread().getContextClassLoader());
    lifeCycle.initialize();

    ClosableClassLoader classLoader = new ClosableClassLoader();
    lifeCycle.initAndAppend(classLoader);

    lifeCycle.shutdown();

    assertThat(classLoader.closed).isTrue();
  }

  private static class ClosableClassLoader extends ClassLoader implements Closeable {

    private boolean closed = false;

    public ClosableClassLoader() {
      super();
    }

    @Override
    public void close() {
      closed = true;
    }
  }
}
