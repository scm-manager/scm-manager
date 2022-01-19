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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

//~--- JDK imports ------------------------------------------------------------

/**
 * The ExplodedSmp object represents an extracted SCM-Manager plugin. The object
 * contains the path to the plugin directory and loads the plugin descriptor.
 * The ExplodedSmp can be created with the {@link #create(java.nio.file.Path)}
 * method.
 *
 * @author Sebastian Sdorra
 */
public final class ExplodedSmp
{

  private static final Logger logger = LoggerFactory.getLogger(ExplodedSmp.class);

  /**
   * Constructs ...
   *
   *
   * @param path
   * @param plugin
   */
  ExplodedSmp(Path path, InstalledPluginDescriptor plugin)
  {
    logger.trace("create exploded scm for plugin {} and dependencies {}", plugin.getInformation().getName(), plugin.getDependencies());
    this.path = path;
    this.plugin = plugin;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new ExplodedSmp object.
   *
   *
   * @param directory directory containing an extracted SCM-Manager plugin.
   *
   * @return ExplodedSmp object
   *
   * @throws PluginException if the path does not contain an plugin descriptor
   *  or the plugin descriptor could not be parsed
   */
  public static ExplodedSmp create(Path directory)
  {
    Path desc = directory.resolve(PluginConstants.FILE_DESCRIPTOR);

    return new ExplodedSmp(directory, Plugins.parsePluginDescriptor(desc));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true} if the exploded smp contains a core plugin
   * @return {@code true} for a core plugin
   * @since 2.30.0
   */
  public boolean isCore() {
    return Files.exists(path.resolve(PluginConstants.FILE_CORE));
  }


  /**
   * Returns the path to the plugin directory.
   *
   *
   * @return to plugin directory
   */
  public Path getPath()
  {
    return path;
  }

  /**
   * Returns parsed plugin descriptor.
   *
   *
   * @return plugin descriptor
   */
  public InstalledPluginDescriptor getPlugin()
  {
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

  //~--- inner classes --------------------------------------------------------

  /**
   * Transforms {@link Path} to {@link ExplodedSmp}.
   */
  public static class PathTransformer implements Function<Path, ExplodedSmp>
  {

    /**
     * Transforms {@link Path} to {@link ExplodedSmp}. The path must contain an
     * extracted SCM-Manager plugin.
     *
     *
     * @param directory directory containing exploded plugin
     *
     * @return exploded smp object
     *
     * @throws PluginException if the path does not contain an extracted
     *  SCM-Manager plugin.
     */
    @Override
    public ExplodedSmp apply(Path directory)
    {
      return ExplodedSmp.create(directory);
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** directory */
  private final Path path;

  /** plugin object */
  private final InstalledPluginDescriptor plugin;
}
