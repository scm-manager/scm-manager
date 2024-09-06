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


import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The ExplodedSmp object represents an extracted SCM-Manager plugin. The object
 * contains the path to the plugin directory and loads the plugin descriptor.
 * The ExplodedSmp can be created with the {@link #create(java.nio.file.Path)}
 * method.
 *
 */
public final class ExplodedSmp {

  private static final Logger logger = LoggerFactory.getLogger(ExplodedSmp.class);

  /**
   * directory
   */
  private final Path path;

  /**
   * plugin object
   */
  private final InstalledPluginDescriptor plugin;

  ExplodedSmp(Path path, InstalledPluginDescriptor plugin) {
    logger.trace("create exploded scm for plugin {} and dependencies {}", plugin.getInformation().getName(), plugin.getDependencies());
    this.path = path;
    this.plugin = plugin;
  }


  /**
   * Creates a new ExplodedSmp object.
   *
   * @param directory directory containing an extracted SCM-Manager plugin.
   * @return ExplodedSmp object
   * @throws PluginException if the path does not contain an plugin descriptor
   *                         or the plugin descriptor could not be parsed
   */
  public static ExplodedSmp create(Path directory) {
    Path desc = directory.resolve(PluginConstants.FILE_DESCRIPTOR);

    return new ExplodedSmp(directory, Plugins.parsePluginDescriptor(desc));
  }


  /**
   * Returns {@code true} if the exploded smp contains a core plugin
   *
   * @since 2.30.0
   */
  public boolean isCore() {
    return Files.exists(path.resolve(PluginConstants.FILE_CORE));
  }


  /**
   * Returns the path to the plugin directory.
   *
   */
  public Path getPath() {
    return path;
  }

  /**
   * Returns parsed plugin descriptor.
   *
   */
  public InstalledPluginDescriptor getPlugin() {
    return plugin;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExplodedSmp that = (ExplodedSmp) o;
    return path.equals(that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path);
  }

  @Override
  public String toString() {
    PluginInformation information = plugin.getInformation();
    return information.getName() + "@" + information.getVersion() + " (" + path + ")";
  }



  /**
   * Transforms {@link Path} to {@link ExplodedSmp}.
   */
  public static class PathTransformer implements Function<Path, ExplodedSmp> {

    /**
     * Transforms {@link Path} to {@link ExplodedSmp}. The path must contain an
     * extracted SCM-Manager plugin.
     *
     * @param directory directory containing exploded plugin
     * @return exploded smp object
     * @throws PluginException if the path does not contain an extracted
     *                         SCM-Manager plugin.
     */
    @Override
    public ExplodedSmp apply(Path directory) {
      return ExplodedSmp.create(directory);
    }
  }

}
