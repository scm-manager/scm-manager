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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.api.rest.RestActionResult;
import sonia.scm.api.rest.RestActionUploadResult;
import sonia.scm.plugin.OverviewPluginPredicate;
import sonia.scm.plugin.PluginConditionFailedException;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginInformationComparator;
import sonia.scm.plugin.PluginManager;

//~--- JDK imports ------------------------------------------------------------

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.io.IOException;
import java.io.InputStream;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * RESTful Web Service Endpoint to manage plugins.
 * 
 * @author Sebastian Sdorra
 */
@Singleton
@Path("plugins")
public class PluginResource
{

  /**
   * the logger for PluginResource
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PluginResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param pluginManager
   */
  @Inject
  public PluginResource(PluginManager pluginManager)
  {
    this.pluginManager = pluginManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Installs a plugin from a package.
   *
   * @param uploadedInputStream
   * 
   * @return
   *
   * @throws IOException
   */
  @POST
  @Path("install-package")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 412, condition = "precondition failed"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Response install(
    /*@FormParam("package")*/ InputStream uploadedInputStream)
    throws IOException
  {
    Response response = null;

    try
    {
      pluginManager.installPackage(uploadedInputStream);
      response = Response.ok(new RestActionUploadResult(true)).build();
    }
    catch (PluginConditionFailedException ex)
    {
      logger.warn(
        "could not install plugin package, because the condition failed", ex);
      response = Response.status(Status.PRECONDITION_FAILED).entity(
        new RestActionResult(false)).build();
    }
    catch (Exception ex)
    {
      logger.warn("plugin installation failed", ex);
      response =
        Response.serverError().entity(new RestActionResult(false)).build();
    }

    return response;
  }

  /**
   * Installs a plugin.
   *
   * @param id id of the plugin to be installed
   *
   * @return
   */
  @POST
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Path("install/{id}")
  public Response install(@PathParam("id") String id)
  {
    pluginManager.install(id);

    // TODO should return 204 no content
    return Response.ok().build();
  }

  /**
   * Installs a plugin from a package. This method is a workaround for ExtJS
   * file upload, which requires text/html as content-type.
   *
   * @param uploadedInputStream
   * @return
   *
   * @throws IOException
   */
  @POST
  @Path("install-package.html")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 412, condition = "precondition failed"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Response installFromUI(
    /*@FormParam("package")*/ InputStream uploadedInputStream)
    throws IOException
  {
    return install(uploadedInputStream);
  }

  /**
   * Uninstalls a plugin.
   *
   * @param id id of the plugin to be uninstalled
   *
   * @return
   */
  @POST
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Path("uninstall/{id}")
  public Response uninstall(@PathParam("id") String id)
  {
    pluginManager.uninstall(id);

    // TODO should return 204 content
    // consider to do a uninstall with a delete
    return Response.ok().build();
  }

  /**
   * Updates a plugin.
   *
   * @param id id of the plugin to be updated
   *
   * @return
   */
  @POST
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Path("update/{id}")
  public Response update(@PathParam("id") String id)
  {
    pluginManager.update(id);

    // TODO should return 204 content
    // consider to do an update with a put
    
    return Response.ok().build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns all plugins.
   *
   * @return all plugins
   */
  @GET
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Collection<PluginInformation> getAll()
  {
    return pluginManager.getAll();
  }

  /**
   * Returns all available plugins.
   *
   * @return all available plugins
   */
  @GET
  @Path("available")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Collection<PluginInformation> getAvailable()
  {
    return pluginManager.getAvailable();
  }

  /**
   * Returns all plugins which are available for update.
   *
   * @return all plugins which are available for update
   */
  @GET
  @Path("updates")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Collection<PluginInformation> getAvailableUpdates()
  {
    return pluginManager.getAvailableUpdates();
  }

  /**
   * Returns all installed plugins.
   *
   * @return all installed plugins
   */
  @GET
  @Path("installed")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Collection<PluginInformation> getInstalled()
  {
    return pluginManager.getInstalled();
  }

  /**
   * Returns all plugins for the overview.
   *
   * @return all plugins for the overview
   */
  @GET
  @Path("overview")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Collection<PluginInformation> getOverview()
  {
    //J-
    List<PluginInformation> plugins = Lists.newArrayList(
      pluginManager.get(OverviewPluginPredicate.INSTANCE)
    );
    //J+

    Collections.sort(plugins, PluginInformationComparator.INSTANCE);

    Iterator<PluginInformation> it = plugins.iterator();
    String last = null;

    while (it.hasNext())
    {
      PluginInformation pi = it.next();
      String id = pi.getId(false);

      if ((last != null) && id.equals(last))
      {
        it.remove();
      }

      last = id;
    }

    return plugins;
  }

  //~--- fields ---------------------------------------------------------------

  /** plugin manager */
  private final PluginManager pluginManager;
}
