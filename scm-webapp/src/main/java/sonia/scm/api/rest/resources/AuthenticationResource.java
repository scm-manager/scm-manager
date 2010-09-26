/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.ScmState;
import sonia.scm.User;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.web.security.Authenticator;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("authentication")
@Singleton
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class AuthenticationResource
{

  /**
   * Method description
   *
   *
   * @param request
   * @param username
   * @param password
   *
   * @return
   */
  @POST
  public ScmState getState(@Context HttpServletRequest request,
                           @FormParam("username") String username,
                           @FormParam("password") String password)
  {
    ScmState state = null;
    User user = authenticator.authenticate(request, username, password);

    if (user != null)
    {
      state = getState(user);
    }
    else
    {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    return state;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @GET
  public ScmState getState(@Context HttpServletRequest request)
  {
    ScmState state = null;
    User user = authenticator.getUser(request);

    if (user != null)
    {
      state = getState(user);
    }
    else
    {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    return state;
  }

  /**
   * Method description
   *
   *
   *
   * @param user
   *
   * @return
   */
  private ScmState getState(User user)
  {
    ScmState state = new ScmState();

    state.setUser(user);
    state.setRepositoryTypes(
        repositoryManger.getTypes().toArray(new RepositoryType[0]));

    return state;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private Authenticator authenticator;

  /** Field description */
  @Inject
  private RepositoryManager repositoryManger;
}
