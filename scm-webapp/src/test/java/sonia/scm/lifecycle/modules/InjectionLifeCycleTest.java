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

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.lifecycle.modules.CloseableModule;
import sonia.scm.Default;
import sonia.scm.EagerSingleton;
import sonia.scm.lifecycle.modules.EagerSingletonModule;
import sonia.scm.lifecycle.modules.InjectionLifeCycle;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
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
