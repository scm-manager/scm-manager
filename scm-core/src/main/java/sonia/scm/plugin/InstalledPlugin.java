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
