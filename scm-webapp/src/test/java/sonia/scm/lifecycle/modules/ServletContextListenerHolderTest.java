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
