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
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.ScmClientConfig;
import sonia.scm.ScmState;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.user.User;
import sonia.scm.web.security.WebSecurityContext;

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
@Singleton
@Path("authentication")
@ExternallyManagedLifecycle
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class AuthenticationResource
{

  /** the logger for AuthenticationResource */
  private static final Logger logger =
    LoggerFactory.getLogger(AuthenticationResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param contextProvider
   * @param configuration
   * @param repositoryManger
   * @param securityContextProvider
   */
  @Inject
  public AuthenticationResource(
          SCMContextProvider contextProvider, ScmConfiguration configuration,
          RepositoryManager repositoryManger,
          Provider<WebSecurityContext> securityContextProvider)
  {
    this.contextProvider = contextProvider;
    this.configuration = configuration;
    this.repositoryManger = repositoryManger;
    this.securityContextProvider = securityContextProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Authenticate a user and return the state of the application.<br />
   * <br />
   * <ul>
   *   <li>200 success</li>
   *   <li>401 unauthorized, the specified username or password is wrong</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param request the current http request
   * @param response the current http response
   * @param username the username for the authentication
   * @param password the password for the authentication
   *
   * @return
   */
  @POST
  @Path("login")
  @TypeHint(ScmState.class)
  public ScmState authenticate(@Context HttpServletRequest request,
                               @Context HttpServletResponse response,
                               @FormParam("username") String username,
                               @FormParam("password") String password)
  {
    ScmState state = null;
    WebSecurityContext securityContext = securityContextProvider.get();
    User user = securityContext.authenticate(request, response, username,
                  password);

    if ((user != null) &&!SCMContext.USER_ANONYMOUS.equals(user.getName()))
    {
      state = createState(securityContext);
    }
    else
    {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    return state;
  }

  /**
   * Logout the current user. Returns the current state of the application,
   * if public access is enabled.<br />
   * <br />
   * <ul>
   *   <li>200 success</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param request the current http request
   * @param response the current http response
   *
   * @return
   */
  @GET
  @Path("logout")
  @TypeHint(ScmState.class)
  public Response logout(@Context HttpServletRequest request,
                         @Context HttpServletResponse response)
  {
    WebSecurityContext securityContext = securityContextProvider.get();

    securityContext.logout(request, response);

    Response resp = null;
    User user = securityContext.getUser();

    if (user != null)
    {
      ScmState state = createState(securityContext);

      resp = Response.ok(state).build();
    }
    else
    {
      resp = Response.ok().build();
    }

    return resp;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the current state of the application.<br />
   * <br />
   * <ul>
   *   <li>200 success</li>
   *   <li>401 unauthorized, user is not authenticated and public access is disabled.</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param request the current http request
   *
   * @return
   */
  @GET
  @TypeHint(ScmState.class)
  public Response getState(@Context HttpServletRequest request)
  {
    Response response = null;
    ScmState state = null;
    WebSecurityContext securityContext = securityContextProvider.get();
    User user = securityContext.getUser();

    if (user != null)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("return state for user {}", user.getName());
      }

      state = createState(securityContext);
      response = Response.ok(state).build();
    }
    else
    {
      response = Response.status(Response.Status.UNAUTHORIZED).build();
    }

    return response;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param securityContext
   *
   * @return
   */
  private ScmState createState(WebSecurityContext securityContext)
  {
    return new ScmState(contextProvider, securityContext,
                        repositoryManger.getConfiguredTypes(),
                        new ScmClientConfig(configuration.getDateFormat(),
                          configuration.isDisableGroupingGrid()));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private SCMContextProvider contextProvider;

  /** Field description */
  private RepositoryManager repositoryManger;

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
