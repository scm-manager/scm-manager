/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
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



package sonia.scm.net;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * The ChildFirstURLClassLoader alters regular ClassLoader delegation and will
 * check the URLs used in its initialization for matching classes before
 * delegating to it's parent. Sometimes also referred to as a
 * ParentLastClassLoader.
 *
 * @author Sebastian Sdorra
 * @since 1.29
 */
public class ChildFirstURLClassLoader extends URLClassLoader
{

  /**
   * Constructs a new ChildFirstURLClassLoader for the specified URLs using the
   * default delegation parent ClassLoader.
   *
   * @param urls the URLs from which to load classes and resources
   */
  public ChildFirstURLClassLoader(URL[] urls)
  {
    super(urls);
  }

  /**
   * Constructs a new ChildFirstURLClassLoader for the specified URLs using the
   * given parent ClassLoader for delegation.
   *
   * @param urls the URLs from which to load classes and resources
   * @param parent the parent class loader for delegation
   */
  public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent)
  {
    super(urls, parent);
  }

  /**
   * Constructs a new URLClassLoader for the specified URLs, parent class
   * loader, and URLStreamHandlerFactory. The parent argument will be used as
   * the parent class loader for delegation. The factory argument will be used
   * as the stream handler factory to obtain protocol handlers when creating
   * new URLs.
   *
   * @param urls the URLs from which to load classes and resources
   * @param parent the parent class loader for delegation
   * @param factory the URLStreamHandlerFactory to use when creating URLs
   */
  public ChildFirstURLClassLoader(URL[] urls, ClassLoader parent,
    URLStreamHandlerFactory factory)
  {
    super(urls, parent, factory);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Loads the class with the specified name. The default implementation of this
   * method searches for classes in the following order:
   * <br />
   * <br />
   * <ol>
   *   <li>
   *     Invoke the {@link ClassLoader#findClass(String)} method to find the
   *     class.
   *   </li>
   *   <li>
   *     Invoke {@link ClassLoader#findLoadedClass(String)} to check if
   *     the class has already been loaded.
   *   </li>
   *   <li>
   *     Invoke the {@link ClassLoader#loadClass(String)} method on the parent
   *     class loader. If the parent is null the class loader built-in to the
   *     virtual machine is used, instead.
   *   </li>
   * </ol>
   *
   * If the class was found using the above steps, and the {@code resolve} flag
   * is true, this method will then invoke the {@link #resolveClass(Class)}
   * method on the resulting Class object. Subclasses of ClassLoader are
   * encouraged to override {@link ClassLoader#findClass(String)}, rather than
   * this method.
   *
   * @param name the name of the class
   * @param resolve if true then resolve the class
   *
   * @return the resulting {@code Class} object
   *
   * @throws ClassNotFoundException if the class could not be found
   */
  @Override
  @SuppressWarnings("unchecked")
  public synchronized Class loadClass(String name, boolean resolve)
    throws ClassNotFoundException
  {
    Class clazz = findLoadedClass(name);

    if (clazz == null)
    {
      try
      {
        clazz = findClass(name);
      }
      catch (ClassNotFoundException e)
      {
        ClassLoader parent = getParent();

        if (parent != null)
        {
          clazz = parent.loadClass(name);
        }
        else
        {
          clazz = getSystemClassLoader().loadClass(name);
        }
      }
    }

    if (resolve)
    {
      resolveClass(clazz);
    }

    return clazz;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * {@inheritDoc }
   */
  @Override
  public URL getResource(String name)
  {
    URL resource = findResource(name);

    if (resource == null)
    {
      ClassLoader parent = getParent();

      if (parent != null)
      {
        resource = parent.getResource(name);
      }
    }

    return resource;
  }
}
