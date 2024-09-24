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

import jakarta.servlet.ServletContext;

import java.net.URL;
import java.util.List;

/**
 * Load resources from {@link ServletContext} and from the installed plugins.
 * The UberWebResourceLoader will first look into the {@link ServletContext} and
 * afterwards it will search the plugin directories.
 *
 * @since 2.0.0
 */
public interface UberWebResourceLoader extends WebResourceLoader
{

  /**
   * Returns all {@link URL} objects for the given path. The method will collect
   * all resources from {@link ServletContext} and all plugin directories which
   * matches the given path. The method will return an empty list, if no url
   * could be found for the given path.
   *
   * @param path resource path
   *
   * @return list of url objects for the given path
   */
  public List<URL> getResources(String path);
}
