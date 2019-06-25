package sonia.scm.lifecycle;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import sonia.scm.CloseableModule;
import sonia.scm.Default;
import sonia.scm.EagerSingletonModule;
import sonia.scm.ServletContextListenerHolder;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.Optional;

class InjectionLifeCycle {

  private final Injector injector;

  InjectionLifeCycle(Injector injector) {
    this.injector = injector;
  }

  void initialize() {
    initializeEagerSingletons();
    initializeServletContextListeners();
  }

  void shutdown() {
    destroyServletContextListeners();
    closeRegisteredCloseables();
  }

  private void initializeServletContextListeners() {
    ServletContextListenerHolder instance = injector.getInstance(ServletContextListenerHolder.class);
    ServletContext context = injector.getInstance(Key.get(ServletContext.class, Default.class));
    instance.contextInitialized(new ServletContextEvent(context));
  }

  private void initializeEagerSingletons() {
    findInstance(EagerSingletonModule.class).ifPresent(m -> m.initialize(injector));
  }

  private void closeRegisteredCloseables() {
    findInstance(CloseableModule.class).ifPresent(CloseableModule::closeAll);
  }

  private void destroyServletContextListeners() {
    ServletContextListenerHolder instance = injector.getInstance(ServletContextListenerHolder.class);
    ServletContext context = injector.getInstance(Key.get(ServletContext.class, Default.class));
    instance.contextDestroyed(new ServletContextEvent(context));
  }

  private <T> Optional<T> findInstance(Class<T> clazz) {
    Binding<T> binding = injector.getExistingBinding(Key.get(clazz));
    if (binding != null) {
      return Optional.of(binding.getProvider().get());
    }
    return Optional.empty();
  }
}
