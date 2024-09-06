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

import java.nio.file.Path;

/**
 * Wrapper for a {@link InstalledPluginDescriptor}. The wrapper holds the directory,
 * {@link ClassLoader} and {@link WebResourceLoader} of a plugin.
 *
 * @since 2.0.0
 */
public final class InstalledPlugin implements Plugin {

  private final ClassLoader classLoader;

  private final Path directory;

  private final InstalledPluginDescriptor descriptor;

  private final WebResourceLoader webResourceLoader;

  private final boolean core;

  private boolean markedForUninstall = false;
  private boolean uninstallable = false;
  public static final String UNINSTALL_MARKER_FILENAME = "uninstall";

  /**
   * Constructs a new plugin wrapper.
   *
   * @param descriptor        wrapped plugin
   * @param classLoader       plugin class loader
   * @param webResourceLoader web resource loader
   * @param directory         plugin directory
   * @param core              marked as core or not
   */
  public InstalledPlugin(InstalledPluginDescriptor descriptor, ClassLoader classLoader,
                         WebResourceLoader webResourceLoader, Path directory, boolean core) {
    this.descriptor = descriptor;
    this.classLoader = classLoader;
    this.webResourceLoader = webResourceLoader;
    this.directory = directory;
    this.core = core;
  }

  /**
   * Returns plugin class loader.
   */
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  /**
   * Returns plugin directory.
   */
  public Path getDirectory() {
    return directory;
  }

  /**
   * Returns the id of the plugin.
   */
  public String getId() {
    return descriptor.getInformation().getId();
  }

  /**
   * Returns the plugin descriptor.
   */
  @Override
  public InstalledPluginDescriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Returns the {@link WebResourceLoader} for this plugin.
   */
  public WebResourceLoader getWebResourceLoader() {
    return webResourceLoader;
  }

  public boolean isCore() {
    return core;
  }

  public boolean isMarkedForUninstall() {
    return markedForUninstall;
  }

  public void setMarkedForUninstall(boolean markedForUninstall) {
    this.markedForUninstall = markedForUninstall;
  }

  public boolean isUninstallable() {
    return uninstallable;
  }

  public void setUninstallable(boolean uninstallable) {
    this.uninstallable = uninstallable;
  }

}
