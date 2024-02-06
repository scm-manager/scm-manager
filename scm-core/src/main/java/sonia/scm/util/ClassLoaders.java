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
    
package sonia.scm.util;


import com.google.common.base.Preconditions;

/**
 * Util methods for {@link ClassLoader}s.
 *
 * @since 1.37
 */
public final class ClassLoaders
{

  private ClassLoaders() {}


  /**
   * Executes a {@link Runnable} with the given {@link ClassLoader} as context 
   * ClassLoader ({@link Thread#setContextClassLoader(ClassLoader)}).
   *
   *
   * @param classLoader ClassLoader for context
   * @param runnable runnable
   * 
   * @since 2.0.0
   */
  public static void executeInContext(ClassLoader classLoader,
    Runnable runnable)
  {
    Preconditions.checkNotNull(classLoader, "ClassLoader is required");
    Preconditions.checkNotNull(runnable, "runnable is required");

    ClassLoader ctxCl = Thread.currentThread().getContextClassLoader();

    Thread.currentThread().setContextClassLoader(ctxCl);

    try
    {
      runnable.run();
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(ctxCl);
    }
  }


  /**
   * Returns the context {@link ClassLoader} from the current {@link Thread}, if
   * the context {@link ClassLoader} is not available the {@link ClassLoader},
   * which has load the given context class, is used.
   *
   *
   * @param contextClass context class
   *
   * @return context class loader
   */
  public static ClassLoader getContextClassLoader(Class<?> contextClass)
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    if (classLoader == null)
    {
      classLoader = contextClass.getClassLoader();
    }

    return classLoader;
  }
}
