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
import com.google.common.collect.Multimap;

import sonia.scm.util.Util;
import sonia.scm.util.XmlUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public final class ExplodedSmp implements Comparable<ExplodedSmp>
{

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param path
   * @param pluginId
   * @param dependencies
   */
  ExplodedSmp(Path path, PluginId pluginId, Collection<String> dependencies)
  {
    this.path = path;
    this.pluginId = pluginId;
    this.dependencies = dependencies;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   *
   * @throws IOException
   */
  public static ExplodedSmp create(Path directory) throws IOException
  {
    ExplodedSmp smp;

    Path descriptor = directory.resolve(PluginConstants.FILE_DESCRIPTOR);

    try (InputStream in = Files.newInputStream(descriptor))
    {
      //J-
      Multimap<String,String> values = XmlUtil.values(in,
        PluginConstants.EL_GROUPID,
        PluginConstants.EL_ARTIFACTID, 
        PluginConstants.EL_VERSION, 
        PluginConstants.EL_DEPENDENCY
      );
      
      PluginId pluginId = new PluginId(
        Util.getFirst(values, PluginConstants.EL_GROUPID),
        Util.getFirst(values, PluginConstants.EL_ARTIFACTID),
        Util.getFirst(values, PluginConstants.EL_VERSION)
      );
      
      smp = new ExplodedSmp(
        directory, 
        pluginId, 
        values.get(PluginConstants.EL_DEPENDENCY)
      );
      //J+
    }

    return smp;
  }

  /**
   * Method description
   *
   *
   * @param o
   *
   * @return
   */
  @Override
  public int compareTo(ExplodedSmp o)
  {
    int result;

    if (dependencies.isEmpty() && o.dependencies.isEmpty())
    {
      result = 0;
    }
    else if (dependencies.isEmpty() &&!o.dependencies.isEmpty())
    {
      result = -1;
    }
    else if (!dependencies.isEmpty() && o.dependencies.isEmpty())
    {
      result = 1;
    }
    else
    {
      String id = pluginId.getIdWithoutVersion();
      String oid = o.pluginId.getIdWithoutVersion();

      if (dependencies.contains(oid) && o.dependencies.contains(id))
      {
        StringBuilder b = new StringBuilder("circular dependency detected: ");

        b.append(id).append(" depends on ").append(oid).append(" and ");
        b.append(oid).append(" depends on ").append(id);

        throw new PluginCircularDependencyException(b.toString());
      }
      else if (dependencies.contains(oid))
      {
        result = 999;
      }
      else if (o.dependencies.contains(id))
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
   * Method description
   *
   *
   * @return
   */
  public Collection<String> getDependencies()
  {
    return dependencies;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Path getPath()
  {
    return path;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PluginId getPluginId()
  {
    return pluginId;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/07/12
   * @author         Enter your name here...
   */
  public static class PathTransformer implements Function<Path, ExplodedSmp>
  {

    /**
     * Method description
     *
     *
     * @param directory
     *
     * @return
     */
    @Override
    public ExplodedSmp apply(Path directory)
    {
      try
      {
        return ExplodedSmp.create(directory);
      }
      catch (IOException ex)
      {
        throw new PluginException("could not create ExplodedSmp", ex);
      }
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Collection<String> dependencies;

  /** Field description */
  private final Path path;

  /** Field description */
  private final PluginId pluginId;
}
