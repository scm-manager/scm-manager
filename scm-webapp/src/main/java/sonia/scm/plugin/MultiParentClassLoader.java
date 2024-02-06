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
    
package sonia.scm.plugin;

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
 */
public class MultiParentClassLoader extends ClassLoader
{
  private final List<ClassLoader> parents;
 
  public MultiParentClassLoader(ClassLoader... parents)
  {
    this(Arrays.asList(parents));
  }

 
  public MultiParentClassLoader(Collection<? extends ClassLoader> parents)
  {
    super(null);
    this.parents = new CopyOnWriteArrayList<>(parents);
  }



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

}
