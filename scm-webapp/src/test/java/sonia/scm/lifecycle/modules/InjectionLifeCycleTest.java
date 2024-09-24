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

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.Default;
import sonia.scm.EagerSingleton;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InjectionLifeCycleTest {

  @Mock
  private ServletContext servletContext;

  @Test
  void shouldInitializeEagerSingletons() {
    Injector injector = initialize(new EagerSingletonModule(), new EagerModule());

    Messenger messenger = injector.getInstance(Messenger.class);
    assertThat(messenger.receive()).isEqualTo("eager baby!");
  }

  @Test
  void shouldNotThrowAnExceptionWithoutEagerSingletons() {
    Injector injector = initialize(new EagerSingletonModule());

    Messenger messenger = injector.getInstance(Messenger.class);
    assertThat(messenger.receive()).isNull();
  }

  @Test
  void shouldInitializeServletContextListeners() {
    Injector injector = initialize(new ServletContextListenerModule());

    Messenger messenger = injector.getInstance(Messenger.class);
    assertThat(messenger.receive()).isEqualTo("+4+2");
  }

  @Test
  void shouldCallDestroyOnServletContextListeners() {
    Injector injector = createInjector(servletContext, new ServletContextListenerModule());

    InjectionLifeCycle lifeCycle = new InjectionLifeCycle(injector);
    lifeCycle.shutdown();

    Messenger messenger = injector.getInstance(Messenger.class);
    assertThat(messenger.receive()).isEqualTo("-4-2");
  }

  @Test
  void shouldCloseInstantiatedCloseables() {
    Injector injector = createInjector(servletContext, new FortyTwoModule(), new CloseableModule());

    injector.getInstance(Two.class);
    injector.getInstance(Four.class);


    InjectionLifeCycle lifeCycle = new InjectionLifeCycle(injector);
    lifeCycle.shutdown();

    Messenger messenger = injector.getInstance(Messenger.class);
    assertThat(messenger.receive()).isEqualTo("42");
  }


  private Injector initialize(Module... modules) {
    Injector injector = createInjector(servletContext, modules);

    InjectionLifeCycle lifeCycle = new InjectionLifeCycle(injector);
    lifeCycle.initialize();

    return injector;
  }

  public static class EagerModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ImEager.class);
    }
  }

  @EagerSingleton
  public static class ImEager {

    @Inject
    public ImEager(Messenger messenger) {
      messenger.send("eager baby!");
    }
  }

  public static class FortyTwoModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(Four.class);
      bind(Two.class);
    }
  }

  @Singleton
  public static class Four implements Closeable {

    private final Messenger messenger;

    @Inject
    public Four(Messenger messenger) {
      this.messenger = messenger;
    }

    @Override
    public void close() {
      messenger.append("4");
    }
  }

  @Singleton
  public static class Two implements Closeable {

    private final Messenger messenger;

    @Inject
    public Two(Messenger messenger) {
      this.messenger = messenger;
    }

    @Override
    public void close() {
      messenger.append("2");
    }
  }

  static Injector createInjector(ServletContext context, Module... modules) {
    List<Module> moduleList = new ArrayList<>();
    moduleList.add(new ServletContextModule(context));
    moduleList.addAll(Arrays.asList(modules));

    return Guice.createInjector(moduleList);
  }

  public static class ServletContextModule extends AbstractModule {

    private final ServletContext servletContext;

    ServletContextModule(ServletContext servletContext) {
      this.servletContext = servletContext;
    }

    @Override
    protected void configure() {
      bind(ServletContext.class).annotatedWith(Default.class).toInstance(servletContext);
    }

  }

  public static class ServletContextListenerModule extends AbstractModule {

    @Override
    protected void configure() {
      Multibinder<ServletContextListener> multibinder = Multibinder.newSetBinder(binder(), ServletContextListener.class);
      multibinder.addBinding().to(AppendingFourServletContextListener.class);
      multibinder.addBinding().to(AppendingTwoServletContextListener.class);
    }
  }

  public static class AppendingFourServletContextListener extends AppendingServletContextListener {

    @Inject
    public AppendingFourServletContextListener(Messenger messenger) {
      super(messenger);
    }

    @Override
    protected String getSign() {
      return "4";
    }
  }

  public static class AppendingTwoServletContextListener extends AppendingServletContextListener {

    @Inject
    public AppendingTwoServletContextListener(Messenger messenger) {
      super(messenger);
    }

    @Override
    protected String getSign() {
      return "2";
    }
  }

  public static abstract class AppendingServletContextListener implements ServletContextListener {

    private final Messenger messenger;

    @Inject
    public AppendingServletContextListener(Messenger messenger) {
      this.messenger = messenger;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
      send("+");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
      send("-");
    }

    private void send(String prefix) {
      messenger.append(prefix + getSign());
    }

    protected abstract String getSign();
  }

  @Singleton
  public static class Messenger {

    private String message;

    void send(String message) {
      this.message = message;
    }

    void append(String messageToAppend) {
      send(Strings.nullToEmpty(message) + messageToAppend);
    }

    String receive() {
      return message;
    }
  }

}
