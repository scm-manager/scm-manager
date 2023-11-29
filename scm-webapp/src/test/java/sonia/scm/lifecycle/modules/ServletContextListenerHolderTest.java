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
    
package sonia.scm.lifecycle.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ServletContextListenerHolderTest {

  @Test
  void shouldInitializeEveryContextListener() {
    CountingListener one = new CountingListener(41);
    CountingListener two = new CountingListener(41);

    ServletContextListenerHolder holder = createHolder(one, two);
    holder.contextInitialized(null);

    assertThat(one.counter).isEqualTo(42);
    assertThat(one.counter).isEqualTo(42);
  }

  @Test
  void shouldDestroyEveryContextListener() {
    CountingListener one = new CountingListener(43);
    CountingListener two = new CountingListener(43);

    ServletContextListenerHolder holder = createHolder(one, two);
    holder.contextDestroyed(null);

    assertThat(one.counter).isEqualTo(42);
    assertThat(one.counter).isEqualTo(42);
  }

  @Test
  void shouldNotFailWithoutServletContextListenerBound(){
    Injector injector = Guice.createInjector();
    ServletContextListenerHolder holder = injector.getInstance(ServletContextListenerHolder.class);
    holder.contextInitialized(null);
    holder.contextDestroyed(null);
  }

  private ServletContextListenerHolder createHolder(CountingListener one, CountingListener two) {
    Injector injector = Guice.createInjector(new ListenerModule(one, two));
    return injector.getInstance(ServletContextListenerHolder.class);
  }

  public static class ListenerModule extends AbstractModule {

    private final Set<ServletContextListener> listeners;

    ListenerModule(ServletContextListener... listeners) {
      this.listeners = new HashSet<>(Arrays.asList(listeners));
    }

    @Override
    protected void configure() {
      bind(new Key<Set<ServletContextListener>>(){}).toInstance(listeners);
    }
  }

  public static class CountingListener implements ServletContextListener {

    private int counter;

    CountingListener(int counter) {
      this.counter = counter;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
      counter++;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
      counter--;
    }
  }
}
