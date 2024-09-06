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
