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
