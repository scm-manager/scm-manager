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

import sonia.scm.SCMContext;
import sonia.scm.cache.CacheManager;
import sonia.scm.installer.HgInstallerFactory;
import sonia.scm.installer.HgPackage;
import sonia.scm.installer.HgPackageReader;
import sonia.scm.installer.HgPackages;
import sonia.scm.net.HttpClient;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
   *
   * @param client
   * @param handler
   * @param pkgReader
   */
  @Inject
  public HgConfigResource(HttpClient client, HgRepositoryHandler handler,
                          HgPackageReader pkgReader)
  {
    this.client = client;
    this.handler = handler;
    this.pkgReader = pkgReader;
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
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public HgConfig autoConfiguration(@Context UriInfo uriInfo)
  {
    return autoConfiguration(uriInfo, null);
  }

  /**
   * Method description
   *
   *
   * @param uriInfo
   * @param config
   *
   * @return
   */
  @POST
  @Path("auto-configuration")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public HgConfig autoConfiguration(@Context UriInfo uriInfo, HgConfig config)
  {
    if (config == null)
    {
      config = new HgConfig();
    }

    handler.doAutoConfiguration(config);

    return handler.getConfig();
  }

  /**
   * Method description
   *
   *
   *
   * @param id
   * @return
   */
  @POST
  @Path("packages/{pkgId}")
  public Response installPackage(@PathParam("pkgId") String id)
  {
    Response response = null;
    HgPackage pkg = pkgReader.getPackage(id);

    if (pkg != null)
    {
      if (HgInstallerFactory.createInstaller().installPackage(client, handler,
              SCMContext.getContext().getBaseDirectory(), pkg))
      {
        response = Response.noContent().build();
      }
      else
      {
        response =
          Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    }
    else
    {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }

    return response;
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
  @Path("packages")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public HgPackages getPackages()
  {
    return pkgReader.getPackages();
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
  private HttpClient client;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HgPackageReader pkgReader;
}
