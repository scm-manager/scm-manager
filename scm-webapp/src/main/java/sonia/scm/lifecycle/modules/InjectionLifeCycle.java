package sonia.scm.lifecycle.modules;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import sonia.scm.Default;
import sonia.scm.lifecycle.LifeCycle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.Optional;

public class InjectionLifeCycle implements LifeCycle {

  private final Injector injector;

  public InjectionLifeCycle(Injector injector) {
    this.injector = injector;
  }

  public void initialize() {
    initializeEagerSingletons();
    initializeServletContextListeners();
  }

  public void shutdown() {
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
