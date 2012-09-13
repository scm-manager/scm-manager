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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.ScmClientConfig;
import sonia.scm.ScmState;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.security.ScmAuthenticationToken;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Collections;

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
   * @param userManager
   * @param securityContextProvider
   */
  @Inject
  public AuthenticationResource(SCMContextProvider contextProvider,
    ScmConfiguration configuration, RepositoryManager repositoryManger,
    UserManager userManager)
  {
    this.contextProvider = contextProvider;
    this.configuration = configuration;
    this.repositoryManger = repositoryManger;
    this.userManager = userManager;
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

    Subject subject = SecurityUtils.getSubject();

    try
    {
      subject.login(new ScmAuthenticationToken(request, response, username,
        password));
      state = createState(subject);
    }
    catch (AuthenticationException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("authentication failed for user ".concat(username), ex);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("authentication failed for user {}", username);
      }

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
    Subject subject = SecurityUtils.getSubject();

    subject.logout();

    Response resp = null;

    // TODO handle anonymous access

    User user = null;

    if (user != null)
    {
      ScmState state = createState(subject);

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
   * This method is an alias of the
   * {@link #getState(javax.servlet.http.HttpServletRequest)} method.
   * The only difference between the methods,
   * is that this one could not be used with basic authentication.<br />
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
  @Path("state")
  @TypeHint(ScmState.class)
  public Response getCurrentState(@Context HttpServletRequest request)
  {
    return getState(request);
  }

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
    Subject subject = SecurityUtils.getSubject();

    if (subject.isAuthenticated())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("return state for user {}", subject.getPrincipal());
      }

      ScmState state = createState(subject);

      response = Response.ok(state).build();
    }
    else if (configuration.isAnonymousAccessEnabled())
    {
      User user = new User(SCMContext.USER_ANONYMOUS, "SCM Anonymous",
                    "scm-anonymous@scm-manager.com");
      ScmState state = createState(user, Collections.EMPTY_LIST);

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
   * @param subject
   *
   * @return
   */
  private ScmState createState(Subject subject)
  {
    PrincipalCollection collection = subject.getPrincipals();
    User user = collection.oneByType(User.class);
    GroupNames groups = collection.oneByType(GroupNames.class);

    return createState(user, groups.getCollection());
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param groups
   *
   * @return
   */
  private ScmState createState(User user, Collection<String> groups)
  {
    return new ScmState(contextProvider, user, groups,
      repositoryManger.getConfiguredTypes(), userManager.getDefaultType(),
      new ScmClientConfig(configuration));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private SCMContextProvider contextProvider;

  /** Field description */
  private RepositoryManager repositoryManger;

  /** Field description */
  private UserManager userManager;
}
