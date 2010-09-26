/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.repository.RepositoryManager;


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
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("config/repositories/git")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class GitConfigResource
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   */
  @Inject
  public GitConfigResource(RepositoryManager repositoryManager)
  {
    repositoryHandler = (GitRepositoryHandler) repositoryManager.getHandler(
      GitRepositoryHandler.TYPE_NAME);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  public GitConfig getConfig()
  {
    GitConfig config = repositoryHandler.getConfig();

    if (config == null)
    {
      config = new GitConfig();
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
  public Response setConfig(@Context UriInfo uriInfo, GitConfig config)
  {
    repositoryHandler.setConfig(config);
    repositoryHandler.storeConfig();

    return Response.created(uriInfo.getRequestUri()).build();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler repositoryHandler;
}
