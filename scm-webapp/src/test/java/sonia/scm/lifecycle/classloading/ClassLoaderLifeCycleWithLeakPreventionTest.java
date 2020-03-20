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
class ClassLoaderLifeCycleWithLeakPreventionTest {

  @Mock
  private ClassLoaderLeakPreventorFactory classLoaderLeakPreventorFactory;

  @Mock
  private ClassLoaderLeakPreventor classLoaderLeakPreventor;

  @Test
  void shouldThrowIllegalStateExceptionWithoutInit() {
    ClassLoaderLifeCycleWithLeakPrevention lifeCycle = new ClassLoaderLifeCycleWithLeakPrevention(Thread.currentThread().getContextClassLoader());
    assertThrows(IllegalStateException.class, lifeCycle::getBootstrapClassLoader);
  }

  @Test
  void shouldThrowIllegalStateExceptionAfterShutdown() {
    ClassLoaderLifeCycleWithLeakPrevention lifeCycle = createMockedLifeCycle();
    lifeCycle.initialize();

    lifeCycle.shutdown();
    assertThrows(IllegalStateException.class, lifeCycle::getBootstrapClassLoader);
  }

  @Test
  void shouldCreateBootstrapClassLoaderOnInit() {
    ClassLoaderLifeCycleWithLeakPrevention lifeCycle = new ClassLoaderLifeCycleWithLeakPrevention(Thread.currentThread().getContextClassLoader());
    lifeCycle.initialize();

    assertThat(lifeCycle.getBootstrapClassLoader()).isNotNull();
  }

  @Test
  void shouldCallTheLeakPreventor() {
    ClassLoaderLifeCycleWithLeakPrevention lifeCycle = createMockedLifeCycle();

    lifeCycle.initialize();
    verify(classLoaderLeakPreventor, times(1)).runPreClassLoaderInitiators();

    lifeCycle.createChildFirstPluginClassLoader(new URL[0], null, "a");
    lifeCycle.createPluginClassLoader(new URL[0], null, "b");
    verify(classLoaderLeakPreventor, times(3)).runPreClassLoaderInitiators();

    lifeCycle.shutdown();
    verify(classLoaderLeakPreventor, times(3)).runCleanUps();
  }

  @Test
  void shouldCloseCloseableClassLoaders() throws IOException {
    // we use URLClassLoader, because we must be sure that the classloader is closable
    URLClassLoader webappClassLoader = spy(new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader()));

    ClassLoaderLifeCycleWithLeakPrevention lifeCycle = createMockedLifeCycle(webappClassLoader);
    lifeCycle.setClassLoaderAppendListener(new ClassLoaderLifeCycleWithLeakPrevention.ClassLoaderAppendListener() {
      @Override
      public <C extends ClassLoader> C apply(C classLoader) {
        return spy(classLoader);
      }
    });
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

  private ClassLoaderLifeCycleWithLeakPrevention createMockedLifeCycle() {
    return createMockedLifeCycle(Thread.currentThread().getContextClassLoader());
  }

  private ClassLoaderLifeCycleWithLeakPrevention createMockedLifeCycle(ClassLoader classLoader) {
    when(classLoaderLeakPreventorFactory.newLeakPreventor(any(ClassLoader.class))).thenReturn(classLoaderLeakPreventor);
    return new ClassLoaderLifeCycleWithLeakPrevention(classLoader, classLoaderLeakPreventorFactory);
  }

}
