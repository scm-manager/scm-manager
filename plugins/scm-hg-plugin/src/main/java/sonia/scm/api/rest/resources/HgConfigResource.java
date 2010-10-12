/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.HgWebConfigWriter;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.ServletContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("config/repositories/hg")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  public HgConfig getConfig()
  {
    HgConfig config = handler.getConfig();

    if (config == null)
    {
      config = new HgConfig();
    }

    return config;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param uriInfo
   * @param servletContext
   * @param config
   *
   * @return
   *
   * @throws IOException
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response setConfig(@Context UriInfo uriInfo,
                            @Context ServletContext servletContext,
                            HgConfig config)
          throws IOException
  {
    handler.setConfig(config);
    handler.storeConfig();
    new HgWebConfigWriter(config).write(servletContext);

    return Response.created(uriInfo.getRequestUri()).build();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgRepositoryHandler handler;
}
