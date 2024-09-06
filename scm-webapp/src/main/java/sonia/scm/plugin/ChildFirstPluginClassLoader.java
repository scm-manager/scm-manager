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


import sonia.scm.net.ChildFirstURLClassLoader;

import java.net.URL;

/**
 * Child first {@link ClassLoader} for SCM-Manager plugins.
 *
 */
public class ChildFirstPluginClassLoader extends ChildFirstURLClassLoader
  implements PluginClassLoader
{

  private final String plugin;

 
  public ChildFirstPluginClassLoader(URL[] urls, ClassLoader parent, String plugin)
  {
    super(urls, parent);
    this.plugin = plugin;
  }

  @Override
  public String toString() {
    return ChildFirstPluginClassLoader.class.getName() + " for plugin " + plugin;
  }
}
