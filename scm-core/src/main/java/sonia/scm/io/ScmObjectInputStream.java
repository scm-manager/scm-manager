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

package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * {@link ObjectInputStream} implementation which uses the context class loader
 * to resolve classes.
 *
 * @author Sebastian Sdorra
 * @since 1.36
 */
public class ScmObjectInputStream extends ObjectInputStream {

  /**
   * the logger for ScmObjectInputStream
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmObjectInputStream.class);

  //~--- constructors ---------------------------------------------------------

  public ScmObjectInputStream(InputStream stream) throws IOException {
    super(stream);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc)
    throws IOException, ClassNotFoundException {
    Class<?> clazz = null;
    ClassLoader classLoader = getClassLoader();

    try {
      clazz = classLoader.loadClass(desc.getName());
    } catch (ClassNotFoundException ex) {
      // do not log the exception, because the class 
      // is mostly found by the parent method.
    }

    if (clazz == null) {
      clazz = super.resolveClass(desc);
    }

    return clazz;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the context class loader if available. If the context class loader
   * is not available the method will fall back to the class loader which has
   * load this class.
   *
   * @return context class loader or default class loader
   */
  private ClassLoader getClassLoader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null) {
      logger.debug("could not find context class loader, fall back to default");
      classLoader = ScmObjectInputStream.class.getClassLoader();
    }

    return classLoader;
  }
}
