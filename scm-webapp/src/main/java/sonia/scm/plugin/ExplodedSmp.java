/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;

//~--- JDK imports ------------------------------------------------------------

import java.nio.file.Path;

import java.util.Set;

/**
 * The ExplodedSmp object represents an extracted SCM-Manager plugin. The object
 * contains the path to the plugin directory and loads the plugin descriptor.
 * The ExplodedSmp can be created with the {@link #create(java.nio.file.Path)}
 * method.
 *
 * @author Sebastian Sdorra
 */
public final class ExplodedSmp implements Comparable<ExplodedSmp>
{

  /**
   * Constructs ...
   *
   *
   * @param path
   * @param pluginId
   * @param dependencies
   * @param plugin
   */
  ExplodedSmp(Path path, Plugin plugin)
  {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public int compareTo(ExplodedSmp o)
  {
    int result;

    Set<String> depends = plugin.getDependencies();
    Set<String> odepends = o.plugin.getDependencies();

    if (depends.isEmpty() && odepends.isEmpty())
    {
      result = 0;
    }
    else if (depends.isEmpty() &&!odepends.isEmpty())
    {
      result = -1;
    }
    else if (!depends.isEmpty() && odepends.isEmpty())
    {
      result = 1;
    }
    else
    {
      String id = plugin.getInformation().getId(false);
      String oid = o.plugin.getInformation().getId(false);

      if (depends.contains(oid) && odepends.contains(id))
      {
        StringBuilder b = new StringBuilder("circular dependency detected: ");

        b.append(id).append(" depends on ").append(oid).append(" and ");
        b.append(oid).append(" depends on ").append(id);

        throw new PluginCircularDependencyException(b.toString());
      }
      else if (depends.contains(oid))
      {
        result = 999;
      }
      else if (odepends.contains(id))
      {
        result = -999;
      }
      else
      {
        result = 0;
      }
    }

    return result;
  }

  //~--- get methods ----------------------------------------------------------

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
  public Plugin getPlugin()
  {
    return plugin;
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
  private final Plugin plugin;
}
