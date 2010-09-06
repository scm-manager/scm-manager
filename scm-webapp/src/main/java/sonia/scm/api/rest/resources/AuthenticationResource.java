/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.RepositoryType;
import sonia.scm.ScmState;

//~--- JDK imports ------------------------------------------------------------

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

    if ("hans".equals(username) && "hans123".equals(password))
    {
      request.getSession(true).setAttribute("auth", username);
      state = getState(username);
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
    String username = (String) request.getSession(true).getAttribute("auth");

    if (username != null)
    {
      state = getState(username);
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
   * @param username
   *
   * @return
   */
  private ScmState getState(String username)
  {
    ScmState state = new ScmState();

    state.setUsername(username);

    RepositoryType[] types = new RepositoryType[] {
                               new RepositoryType("hg", "Mercurial"),
                               new RepositoryType("svn", "Subversion"),
                               new RepositoryType("git", "Git") };

    state.setRepositoryTypes(types);

    return state;
  }
}
