/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest.resources;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Singleton;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
  public Response authenticate(@Context HttpServletRequest request,
                               @FormParam("username") String username,
                               @FormParam("password") String password)
  {
    Response response = null;

    if ("hans".equals(username) && "hans123".equals(password))
    {
      request.getSession(true).setAttribute("auth", Boolean.TRUE);
      response = Response.ok().build();
    }
    else
    {
      response = Response.status(Response.Status.UNAUTHORIZED).build();
    }

    return response;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @GET
  public Response isAuthenticated(@Context HttpServletRequest request)
  {
    Response response = null;

    if (request.getSession(true).getAttribute("auth") != null)
    {
      System.out.println( "authenticated" );

      response = Response.ok().build();
    }
    else
    {
      response = Response.status(Response.Status.UNAUTHORIZED).build();
    }

    return response;
  }
}
