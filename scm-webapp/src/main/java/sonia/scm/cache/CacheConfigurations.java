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

package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import static java.util.Collections.emptyIterator;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public final class CacheConfigurations
{

  /**
   * the logger for CacheConfigurations
   */
  private static final Logger logger =
    LoggerFactory.getLogger(CacheConfigurations.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private CacheConfigurations() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param loadingClass
   * @param resource
   *
   * @return
   */
  public static Iterator<URL> findModuleResources(Class<?> loadingClass,
    String resource)
  {
    Iterator<URL> it = null;

    try
    {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      if (classLoader == null)
      {
        classLoader = loadingClass.getClassLoader();
      }

      Enumeration<URL> enm = classLoader.getResources(resource);

      if (enm != null)
      {
        it = Iterators.forEnumeration(enm);
      }

    }
    catch (IOException ex)
    {
      logger.error("could not read module resources", ex);
    }

    if (it == null)
    {
      it = emptyIterator();
    }

    return it;
  }
}
