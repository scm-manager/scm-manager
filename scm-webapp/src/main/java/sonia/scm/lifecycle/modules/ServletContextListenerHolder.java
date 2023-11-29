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

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ServletContextListenerHolder implements ServletContextListener {

  static class ListenerHolder {
    @Inject(optional = true)
    private Set<ServletContextListener> listenerSet;

    private Set<ServletContextListener> getListenerSet() {
      if (listenerSet == null) {
        return Collections.emptySet();
      }
      return listenerSet;
    }
  }

  private final Set<ServletContextListener> listenerSet;

  @Inject
  public ServletContextListenerHolder(ListenerHolder listeners)
  {
    this.listenerSet = listeners.getListenerSet();
  }


  @Override
  public void contextInitialized(ServletContextEvent sce) {
    listenerSet.forEach(listener -> listener.contextInitialized(sce));
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    listenerSet.forEach(listener -> listener.contextDestroyed(sce));
  }
}
