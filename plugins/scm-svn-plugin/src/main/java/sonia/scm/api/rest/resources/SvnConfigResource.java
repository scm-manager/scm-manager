/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.SvnConfig;
import sonia.scm.repository.SvnRepositoryHandler;

//~--- JDK imports ------------------------------------------------------------

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
@Path("config/repositories/svn")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class SvnConfigResource
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   */
  @Inject
  public SvnConfigResource(RepositoryManager repositoryManager)
  {
    repositoryHandler = (SvnRepositoryHandler) repositoryManager.getHandler(
      SvnRepositoryHandler.TYPE_NAME);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  public SvnConfig getConfig()
  {
    SvnConfig config = repositoryHandler.getConfig();

    if (config == null)
    {
      config = new SvnConfig();
      repositoryHandler.setConfig(config);
    }

    return config;
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
   */
  @POST
  @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public Response setConfig(@Context UriInfo uriInfo, SvnConfig config)
  {
    repositoryHandler.setConfig(config);
    repositoryHandler.storeConfig();

    return Response.created(uriInfo.getRequestUri()).build();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SvnRepositoryHandler repositoryHandler;
}
