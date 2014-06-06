/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;

/**
 * Util methods for {@link ClassLoader}s.
 *
 * @author Sebastian Sdorra
 * @since 1.37
 */
public final class ClassLoaders
{

  /**
   * Constructs ...
   *
   */
  private ClassLoaders() {}

  //~--- methods --------------------------------------------------------------

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

  //~--- get methods ----------------------------------------------------------

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
