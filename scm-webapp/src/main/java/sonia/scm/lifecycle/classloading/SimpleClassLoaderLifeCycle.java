/**
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Creates and shutdown SCM-Manager ClassLoaders without ClassLoader leak detection.
 */
class SimpleClassLoaderLifeCycle extends ClassLoaderLifeCycle {

  static final String NAME = "simple";

  private static final Logger LOG = LoggerFactory.getLogger(SimpleClassLoaderLifeCycle.class);

  private Deque<ClassLoader> classLoaders = new ArrayDeque<>();

  SimpleClassLoaderLifeCycle(ClassLoader webappClassLoader) {
    super(webappClassLoader);
  }

  @Override
  protected <T extends ClassLoader> T initAndAppend(T classLoader) {
    LOG.debug("init classloader {}", classLoader);
    classLoaders.push(classLoader);
    return classLoader;
  }

  @Override
  protected void shutdownClassLoaders() {
    ClassLoader classLoader = classLoaders.poll();
    while (classLoader != null) {
      shutdown(classLoader);
      classLoader = classLoaders.poll();
    }
    // be sure it is realy empty
    classLoaders.clear();
    classLoaders = new ArrayDeque<>();
  }

  private void shutdown(ClassLoader classLoader) {
    LOG.debug("shutdown classloader {}", classLoader);
    if (classLoader instanceof Closeable) {
      LOG.trace("close classloader {}", classLoader);
      try {
        ((Closeable) classLoader).close();
      } catch (IOException e) {
        LOG.warn("failed to close classloader", e);
      }
    }
  }
}
