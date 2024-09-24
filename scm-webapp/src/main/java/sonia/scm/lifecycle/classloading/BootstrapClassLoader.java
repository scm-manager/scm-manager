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

package sonia.scm.lifecycle.classloading;

/**
 * This ClassLoader is mainly a wrapper around the web application class loader and its goal is to make it easier to
 * find it in a heap dump.
 */
class BootstrapClassLoader extends ClassLoader {

  /**
   * Marker to find a BootstrapClassLoader, which is already shutdown.
   */
  private boolean shutdown = false;

  BootstrapClassLoader(ClassLoader webappClassLoader) {
    super(webappClassLoader);
  }

  /**
   * Returns {@code true} if the classloader was shutdown.
   *
   * @return {@code true} if the classloader was shutdown
   */
  boolean isShutdown() {
    return shutdown;
  }

  /**
   * Mark the class loader as shutdown.
   */
  void markAsShutdown() {
    shutdown = true;
  }
}
