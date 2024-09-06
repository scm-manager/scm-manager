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


import java.util.Collection;


public interface PluginLoader
{

  
  public ExtensionProcessor getExtensionProcessor();

  
  public Collection<ScmModule> getInstalledModules();

  
  public Collection<InstalledPlugin> getInstalledPlugins();

  /**
   * Returns a {@link ClassLoader} which is able to load classes and resources
   * from the webapp and all installed plugins.
   *
   *
   * @return uber classloader
   *
   * @since 2.0.0
   */
  public ClassLoader getUberClassLoader();

  /**
   * Returns a {@link WebResourceLoader} which is able to load web resources
   * from the webapp and all installed plugins.
   *
   *
   * @return uber webresourceloader
   *
   * @since 2.0.0
   */
  public UberWebResourceLoader getUberWebResourceLoader();
}
