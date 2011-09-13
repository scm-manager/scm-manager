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



package sonia.scm.plugin.rest;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import sonia.scm.plugin.PluginBackend;
import sonia.scm.plugin.PluginFilter;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.view.Viewable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("/detail/{groupId}/{artifactId}.html")
public class DetailResource extends ViewableResource
{

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param backend
   */
  @Inject
  public DetailResource(ServletContext context, PluginBackend backend)
  {
    super(context);
    this.backend = backend;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param groupId
   * @param artifactId
   * @param snapshot
   *
   * @return
   */
  @GET
  public Viewable getPluginDetails(@PathParam("groupId") String groupId,
                                   @PathParam("artifactId") String artifactId,
                                   @DefaultValue("false")
  @QueryParam("snapshot") boolean snapshot)
  {
    List<PluginInformation> pluginVersions = getPluginVersions(groupId,
                                               artifactId);

    if (Util.isEmpty(pluginVersions))
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    Collections.sort(pluginVersions, PluginInformationComparator.INSTANCE);
    pluginVersions = filterSameVersions(pluginVersions);

    PluginInformation latest = pluginVersions.get(0);

    if (!snapshot)
    {
      pluginVersions = filterSnapshots(pluginVersions);
    }

    Map<String, Object> vars = createVarMap(latest.getName());

    vars.put("latest", latest);
    vars.put("versions", pluginVersions);

    return new Viewable("/detail", vars);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param plugins
   *
   * @return
   */
  private List<PluginInformation> filterSameVersions(
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
  private List<PluginInformation> filterSnapshots(
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param groupId
   * @param artifactId
   *
   * @return
   */
  private List<PluginInformation> getPluginVersions(final String groupId,
          final String artifactId)
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PluginBackend backend;
}
