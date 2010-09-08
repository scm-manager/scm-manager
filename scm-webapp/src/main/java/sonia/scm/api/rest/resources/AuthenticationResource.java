/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.RepositoryType;
import sonia.scm.ScmState;
import sonia.scm.User;
import sonia.scm.security.Authenticator;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Inject;
import javax.inject.Singleton;

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
@Singleton
@Path("authentication")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class AuthenticationResource
{

  /** Field description */
  private static final RepositoryType[] types = new RepositoryType[] {
                                                  new RepositoryType("hg",
                                                    "Mercurial"),
          new RepositoryType("svn", "Subversion"),
          new RepositoryType("git", "Git") };

  //~--- get methods ----------------------------------------------------------

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
    state.setRepositoryTypes(types);

    return state;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private Authenticator authenticator;
}
