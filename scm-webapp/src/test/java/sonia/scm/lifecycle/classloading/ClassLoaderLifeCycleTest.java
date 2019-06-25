package sonia.scm.lifecycle.classloading;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventor;
import se.jiderhamn.classloader.leak.prevention.ClassLoaderLeakPreventorFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassLoaderLifeCycleTest {

  @Mock
  private ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory;

  @Mock
  private ClassLoaderLeakPreventor classLoaderLeakPreventor;

  @Test
  void shouldThrowIllegalStateExceptionWithoutInit() {
    ClassLoaderLifeCycle lifeCycle = ClassLoaderLifeCycle.create();
    assertThrows(IllegalStateException.class, lifeCycle::getBootstrapClassLoader);
  }

  @Test
  void shouldThrowIllegalStateExceptionAfterShutdown() {
    ClassLoaderLifeCycle lifeCycle = createMockedLifeCycle();
    lifeCycle.initialize();

    lifeCycle.shutdown();
    assertThrows(IllegalStateException.class, lifeCycle::getBootstrapClassLoader);
  }

  @Test
  void shouldCreateBootstrapClassLoaderOnInit() {
    ClassLoaderLifeCycle lifeCycle = ClassLoaderLifeCycle.create();
    lifeCycle.initialize();

    assertThat(lifeCycle.getBootstrapClassLoader()).isNotNull();
  }

  @Test
  void shouldCallTheLeakPreventor() {
    ClassLoaderLifeCycle lifeCycle = createMockedLifeCycle();

    lifeCycle.initialize();
    verify(classLoaderLeakPreventor, times(2)).runPreClassLoaderInitiators();

    lifeCycle.createChildFirstPluginClassLoader(new URL[0], null, "a");
    lifeCycle.createPluginClassLoader(new URL[0], null, "b");
    verify(classLoaderLeakPreventor, times(4)).runPreClassLoaderInitiators();

    lifeCycle.shutdown();
    verify(classLoaderLeakPreventor, times(4)).runCleanUps();
  }

  @Test
  void shouldCloseCloseableClassLoaders() throws IOException {
    // we use URLClassLoader, because we must be sure that the classloader is closable
    URLClassLoader webappClassLoader = spy(new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader()));

    ClassLoaderLifeCycle lifeCycle = createMockedLifeCycle(webappClassLoader);
    lifeCycle.setClassLoaderAppendListener(c -> spy(c));
    lifeCycle.initialize();

    ClassLoader pluginA = lifeCycle.createChildFirstPluginClassLoader(new URL[0], null, "a");
    ClassLoader pluginB = lifeCycle.createPluginClassLoader(new URL[0], null, "b");

    lifeCycle.shutdown();

    closed(pluginB);
    closed(pluginA);

    neverClosed(webappClassLoader);
  }

  private void neverClosed(Object object) throws IOException {
    Closeable closeable = closeable(object);
    verify(closeable, never()).close();
  }

  private void closed(Object object) throws IOException {
    Closeable closeable = closeable(object);
    verify(closeable).close();
  }

  private Closeable closeable(Object object) {
    assertThat(object).isInstanceOf(Closeable.class);
    return (Closeable) object;
  }

  private ClassLoaderLifeCycle createMockedLifeCycle() {
    return createMockedLifeCycle(Thread.currentThread().getContextClassLoader());
  }

  private ClassLoaderLifeCycle createMockedLifeCycle(ClassLoader classLoader) {
    when(classLoaderLeakPreventorFactory.newLeakPreventor(any(ClassLoader.class))).thenReturn(classLoaderLeakPreventor);
    return new ClassLoaderLifeCycle(classLoader, classLoaderLeakPreventorFactory);
  }

}
