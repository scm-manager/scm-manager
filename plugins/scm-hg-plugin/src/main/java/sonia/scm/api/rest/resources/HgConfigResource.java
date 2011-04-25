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

import sonia.scm.installer.HgInstallerFactory;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.HgWebConfigWriter;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("config/repositories/hg")
public class HgConfigResource
{

  /**
   * Constructs ...
   *
   *
   *
   * @param handler
   */
  @Inject
  public HgConfigResource(HgRepositoryHandler handler)
  {
    this.handler = handler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param uriInfo
   *
   * @return
   */
  @POST
  @Path("auto-configuration")
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public HgConfig autoConfiguration(@Context UriInfo uriInfo)
  {
    handler.setConfig(null);
    handler.doAutoConfiguration();

    return handler.getConfig();
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
  public HgConfig getConfig()
  {
    HgConfig config = handler.getConfig();

    if (config == null)
    {
      config = new HgConfig();
    }

    return config;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Path("installations/hg")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public InstallationsResponse getHgInstallations()
  {
    List<String> installations =
      HgInstallerFactory.createInstaller().getHgInstallations();

    return new InstallationsResponse(installations);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  @Path("installations/python")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public InstallationsResponse getPythonInstallations()
  {
    List<String> installations =
      HgInstallerFactory.createInstaller().getPythonInstallations();

    return new InstallationsResponse(installations);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param uriInfo
   * @param config
   *
   * @return
   *
   * @throws IOException
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response setConfig(@Context UriInfo uriInfo, HgConfig config)
          throws IOException
  {
    handler.setConfig(config);
    handler.storeConfig();
    new HgWebConfigWriter(config).write();

    return Response.created(uriInfo.getRequestUri()).build();
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 11/04/25
   * @author         Enter your name here...
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "installations")
  public static class InstallationsResponse
  {

    /**
     * Constructs ...
     *
     */
    public InstallationsResponse() {}

    /**
     * Constructs ...
     *
     *
     * @param paths
     */
    public InstallationsResponse(List<String> paths)
    {
      this.paths = paths;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public List<String> getPaths()
    {
      return paths;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param paths
     */
    public void setPaths(List<String> paths)
    {
      this.paths = paths;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    @XmlElement(name = "path")
    private List<String> paths;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;
}
