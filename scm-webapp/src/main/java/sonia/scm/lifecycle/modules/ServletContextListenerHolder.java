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


import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.Collections;
import java.util.Set;


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
