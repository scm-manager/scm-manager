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
