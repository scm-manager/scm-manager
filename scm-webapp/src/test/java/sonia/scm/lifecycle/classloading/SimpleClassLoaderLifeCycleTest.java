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
