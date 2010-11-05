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

import sonia.scm.ScmState;
import sonia.scm.Type;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.user.User;
import sonia.scm.web.security.SecurityContext;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
   * @param response
   *
   * @return
   */
  @GET
  @Path("logout")
  public Response logout(@Context HttpServletRequest request,
                         @Context HttpServletResponse response)
  {
    securityContext.logout(request, response);

    return Response.ok().build();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param username
   * @param password
   *
   * @return
   */
  @POST
  @Path("login")
  public ScmState getState(@Context HttpServletRequest request,
                           @Context HttpServletResponse response,
                           @FormParam("username") String username,
                           @FormParam("password") String password)
  {
    ScmState state = null;
    User user = securityContext.authenticate(request, response, username,
                  password);

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
    User user = securityContext.getUser();

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
    state.setRepositoryTypes(repositoryManger.getTypes().toArray(new Type[0]));

    return state;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private RepositoryManager repositoryManger;

  /** Field description */
  @Inject
  private SecurityContext securityContext;
}
