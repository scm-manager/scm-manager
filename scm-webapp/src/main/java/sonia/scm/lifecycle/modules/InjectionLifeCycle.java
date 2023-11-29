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

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import sonia.scm.Default;
import sonia.scm.lifecycle.LifeCycle;

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
