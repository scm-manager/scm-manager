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

/**
 * The WebResourceLoader is able to load web resources. The resources are loaded
 * from a plugin webapp directory or from the {@link ServletContext}, according
 * to its implementation. The {@link UberWebResourceLoader} is able to load
 * resources from the {@link ServletContext} and all plugin directories.
 *
 * @since 2.0.0
 */
public interface WebResourceLoader
{

  /**
   * Returns a {@link URL} for the given path. The method will return null if no
   * resources could be found for the given path.
   *
   * Note: The path is a web path and uses "/" as path separator
   *
   * @param path resource path
   *
   * @return url object for the given path or null
   */
  URL getResource(String path);
}
