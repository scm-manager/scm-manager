package sonia.scm.lifecycle.classloading;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassLoaderLifeCycleTest {

  @Test
  void shouldCreateSimpleClassLoader() {
    System.setProperty(ClassLoaderLifeCycle.PROPERTY, SimpleClassLoaderLifeCycle.NAME);
    try {
      ClassLoaderLifeCycle classLoaderLifeCycle = ClassLoaderLifeCycle.create();
      assertThat(classLoaderLifeCycle).isInstanceOf(SimpleClassLoaderLifeCycle.class);
    } finally {
      System.clearProperty(ClassLoaderLifeCycle.PROPERTY);
    }
  }

  @Test
  void shouldCreateDefaultClassLoader() {
    ClassLoaderLifeCycle classLoaderLifeCycle = ClassLoaderLifeCycle.create();
    assertThat(classLoaderLifeCycle).isInstanceOf(ClassLoaderLifeCycleWithLeakPrevention.class);
  }
}
