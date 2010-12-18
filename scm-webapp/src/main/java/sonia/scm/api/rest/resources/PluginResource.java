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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.plugin.DefaultPluginManager;
import sonia.scm.plugin.PluginInformation;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("plugins")
public class PluginResource
{

  /**
   * Constructs ...
   *
   *
   * @param pluginManager
   */
  @Inject
  public PluginResource(DefaultPluginManager pluginManager)
  {
    this.pluginManager = pluginManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @POST
  @Path("install/{id}")
  public Response install(@PathParam("id") String id)
  {
    pluginManager.install(id);

    return Response.ok().build();
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @POST
  @Path("uninstall/{id}")
  public Response uninstall(@PathParam("id") String id)
  {
    pluginManager.uninstall(id);

    return Response.ok().build();
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @POST
  @Path("update/{id}")
  public Response update(@PathParam("id") String id)
  {
    pluginManager.update(id);

    return Response.ok().build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public PluginInformation[] getAll()
  {
    return getArray(pluginManager.getAll());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Path("available")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public PluginInformation[] getAvailable()
  {
    return getArray(pluginManager.getAvailable());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Path("updates")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public PluginInformation[] getAvailableUpdates()
  {
    return getArray(pluginManager.getAvailableUpdates());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Path("installed")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public PluginInformation[] getInstalled()
  {
    return getArray(pluginManager.getInstalled());
  }

  /**
   * Method description
   *
   *
   * @param pluginCollection
   *
   * @return
   */
  private PluginInformation[] getArray(
          Collection<PluginInformation> pluginCollection)
  {
    PluginInformation[] plugins = null;

    if (pluginCollection != null)
    {
      plugins = pluginCollection.toArray(new PluginInformation[0]);
    }
    else
    {
      plugins = new PluginInformation[0];
    }

    return plugins;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private DefaultPluginManager pluginManager;
}
