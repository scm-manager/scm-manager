/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.LinkTextParser;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class PluginUtil
{

  /**
   * Method description
   *
   *
   * @param plugins
   *
   * @return
   */
  public static List<PluginInformation> filterSameVersions(
          List<PluginInformation> plugins)
  {
    List<PluginInformation> filteredPlugins =
      new ArrayList<PluginInformation>();
    String version = "";

    for (PluginInformation plugin : plugins)
    {
      if (!version.equals(plugin.getVersion()))
      {
        version = plugin.getVersion();
        filteredPlugins.add(plugin);
      }
    }

    return filteredPlugins;
  }

  /**
   * Method description
   *
   *
   * @param allVersions
   *
   * @return
   */
  public static List<PluginInformation> filterSnapshots(
          List<PluginInformation> allVersions)
  {
    List<PluginInformation> filtered = new ArrayList<PluginInformation>();

    for (PluginInformation plugin : allVersions)
    {
      if (!plugin.getVersion().contains("SNAPSHOT"))
      {
        filtered.add(plugin);
      }
    }

    return filtered;
  }

  /**
   * Method description
   *
   *
   * @param plugin
   *
   * @return
   */
  public static PluginInformation prepareForView(PluginInformation plugin)
  {
    String description = plugin.getDescription();

    if (Util.isNotEmpty(description))
    {
      plugin = plugin.clone();
      description = LinkTextParser.parseText(description);
      plugin.setDescription(description);
    }

    return plugin;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param backend
   * @param groupId
   * @param artifactId
   *
   * @return
   */
  public static List<PluginInformation> getFilteredPluginVersions(
          PluginBackend backend, String groupId, String artifactId)
  {
    List<PluginInformation> pluginVersions =
      PluginUtil.getPluginVersions(backend, groupId, artifactId);

    if (Util.isNotEmpty(pluginVersions))
    {
      Collections.sort(pluginVersions,
                       PluginInformationNameComparator.INSTANCE);
      pluginVersions = PluginUtil.filterSameVersions(pluginVersions);
    }

    return pluginVersions;
  }

  /**
   * Method description
   *
   *
   * @param backend
   * @param groupId
   * @param artifactId
   *
   * @return
   */
  public static PluginInformation getLatestPluginVersion(PluginBackend backend,
          String groupId, String artifactId)
  {
    return getFilteredPluginVersions(backend, groupId, artifactId).get(0);
  }

  /**
   * Method description
   *
   *
   *
   * @param backend
   * @param groupId
   * @param artifactId
   *
   * @return
   */
  public static List<PluginInformation> getPluginVersions(
          PluginBackend backend, final String groupId, final String artifactId)
  {
    List<PluginInformation> pluginVersions =
      backend.getPlugins(new PluginFilter()
    {
      @Override
      public boolean accept(PluginInformation plugin)
      {
        return groupId.equals(plugin.getGroupId())
               && artifactId.equals(plugin.getArtifactId());
      }
    });

    return pluginVersions;
  }
}
