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



package sonia.scm.plugin;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.net.URL;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TODO add caching
 *
 * @author Sebastian Sdorra
 */
public class MultiParentClassLoader extends ClassLoader
{

  /**
   * Constructs ...
   *
   *
   * @param parents
   */
  public MultiParentClassLoader(ClassLoader... parents)
  {
    this(Arrays.asList(parents));
  }

  /**
   * Constructs ...
   *
   *
   * @param parents
   */
  public MultiParentClassLoader(Collection<? extends ClassLoader> parents)
  {
    super(null);
    this.parents = new CopyOnWriteArrayList<ClassLoader>(parents);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @Override
  public URL getResource(String name)
  {
    for (ClassLoader parent : parents)
    {
      URL resource = parent.getResource(name);

      if (resource != null)
      {
        return resource;
      }
    }

    return null;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public Enumeration<URL> getResources(String name) throws IOException
  {
    Set<URL> resources = new LinkedHashSet<>();

    for (ClassLoader parent : parents)
    {
      Enumeration<URL> parentResources = parent.getResources(name);

      while (parentResources.hasMoreElements())
      {
        resources.add(parentResources.nextElement());
      }
    }

    return Collections.enumeration(resources);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   * @param resolve
   *
   * @return
   *
   * @throws ClassNotFoundException
   */
  @Override
  protected Class<?> loadClass(String name, boolean resolve)
    throws ClassNotFoundException
  {
    for (ClassLoader parent : parents)
    {
      try
      {
        return parent.loadClass(name);
      }
      catch (ClassNotFoundException e)
      {

        // Expected
      }
    }

    throw new ClassNotFoundException(String.format("%s not found.", name));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final List<ClassLoader> parents;
}
