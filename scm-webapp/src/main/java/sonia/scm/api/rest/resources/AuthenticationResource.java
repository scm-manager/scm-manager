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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authz.Permission;
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
import sonia.scm.api.rest.RestActionResult;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.security.AuthorizationCollector;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.Role;
import sonia.scm.security.SecuritySystem;
import sonia.scm.security.StringablePermission;
import sonia.scm.security.Tokens;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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

  //~--- constant enums -------------------------------------------------------

  /**
   * Enum description
   *
   */
  private static enum WUIAuthenticationFailure { LOCKED, TEMPORARY_LOCKED,
    WRONG_CREDENTIALS; }

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param contextProvider
   * @param configuration
   * @param repositoryManger
   * @param userManager
   * @param securitySystem
   * @param collector
   */
  @Inject
  public AuthenticationResource(SCMContextProvider contextProvider,
    ScmConfiguration configuration, RepositoryManager repositoryManger,
    UserManager userManager, SecuritySystem securitySystem,
    AuthorizationCollector collector)
  {
    this.contextProvider = contextProvider;
    this.configuration = configuration;
    this.repositoryManger = repositoryManger;
    this.userManager = userManager;
    this.securitySystem = securitySystem;
    this.permissionCollector = collector;
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
   * @param username the username for the authentication
   * @param password the password for the authentication
   * @param rememberMe true to remember the user across sessions
   *
   * @return
   */
  @POST
  @Path("login")
  @TypeHint(ScmState.class)
  public Response authenticate(@Context HttpServletRequest request,
    @FormParam("username") String username,
    @FormParam("password") String password, @FormParam("rememberMe")
  @DefaultValue("false") boolean rememberMe)
  {
    Response response;
    Subject subject = SecurityUtils.getSubject();

    try
    {
      subject.login(Tokens.createAuthenticationToken(request, username,
        password, rememberMe));
      response = Response.ok(createState(subject)).build();
    }
    catch (DisabledAccountException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace(
          "authentication failed, account user ".concat(username).concat(
            " is locked"), ex);
      }
      else
      {
        logger.warn("authentication failed, account {} is locked", username);
      }

      response = handleFailedAuthentication(request, ex,
        Response.Status.FORBIDDEN, WUIAuthenticationFailure.LOCKED);
    }
    catch (ExcessiveAttemptsException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace(
          "authentication failed, account user ".concat(username).concat(
            " is temporary locked"), ex);
      }
      else
      {
        logger.warn("authentication failed, account {} is temporary locked",
          username);
      }

      response = handleFailedAuthentication(request, ex,
        Response.Status.FORBIDDEN, WUIAuthenticationFailure.TEMPORARY_LOCKED);
    }
    catch (AuthenticationException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("authentication failed for user ".concat(username), ex);
      }
      else
      {
        logger.warn("authentication failed for user {}", username);
      }

      response = handleFailedAuthentication(request, ex,
        Response.Status.UNAUTHORIZED,
        WUIAuthenticationFailure.WRONG_CREDENTIALS);
    }

    return response;
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

    Response resp;

    if (configuration.isAnonymousAccessEnabled())
    {

      resp = Response.ok(createAnonymousState()).build();
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
    Response response;
    Subject subject = SecurityUtils.getSubject();

    if (subject.isAuthenticated() || subject.isRemembered())
    {
      if (logger.isDebugEnabled())
      {
        String auth = subject.isRemembered()
          ? "remembered"
          : "authenticated";

        logger.debug("return state for {} user {}", auth,
          subject.getPrincipal());
      }

      ScmState state = createState(subject);

      response = Response.ok(state).build();
    }
    else if (configuration.isAnonymousAccessEnabled())
    {

      response = Response.ok(createAnonymousState()).build();
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
   * @return
   */
  private ScmState createAnonymousState()
  {
    return createState(SCMContext.ANONYMOUS, Collections.EMPTY_LIST,
      Collections.EMPTY_LIST, Collections.EMPTY_LIST);
  }

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

    List<PermissionDescriptor> ap = Collections.EMPTY_LIST;

    if (subject.hasRole(Role.ADMIN))
    {
      ap = securitySystem.getAvailablePermissions();
    }

    Builder<String> builder = ImmutableList.builder();

    for (Permission p : permissionCollector.collect().getObjectPermissions())
    {
      if (p instanceof StringablePermission)
      {
        builder.add(((StringablePermission) p).getAsString());
      }

    }

    return createState(user, groups.getCollection(), builder.build(), ap);
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param groups
   * @param assignedPermissions
   * @param availablePermissions
   *
   * @return
   */
  private ScmState createState(User user, Collection<String> groups,
    List<String> assignedPermissions,
    List<PermissionDescriptor> availablePermissions)
  {
    return new ScmState(contextProvider, user, groups,
      repositoryManger.getConfiguredTypes(), userManager.getDefaultType(),
      new ScmClientConfig(configuration), assignedPermissions,
      availablePermissions);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param ex
   * @param status
   * @param failure
   *
   * @return
   */
  private Response handleFailedAuthentication(HttpServletRequest request,
    AuthenticationException ex, Response.Status status,
    WUIAuthenticationFailure failure)
  {
    Response response;

    if (HttpUtil.isWUIRequest(request))
    {
      response = Response.ok(new WUIAuthenticationFailedResult(failure,
        ex.getMessage())).build();
    }
    else
    {
      response = Response.status(status).build();
    }

    return response;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/09/28
   * @author         Enter your name here...
   */
  @XmlRootElement(name = "result")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static final class WUIAuthenticationFailedResult
    extends RestActionResult
  {

    /**
     * Constructs ...
     *
     *
     * @param failure
     * @param mesage
     */
    public WUIAuthenticationFailedResult(WUIAuthenticationFailure failure,
      String mesage)
    {
      super(false);
      this.failure = failure;
      this.mesage = mesage;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public WUIAuthenticationFailure getFailure()
    {
      return failure;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getMesage()
    {
      return mesage;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final WUIAuthenticationFailure failure;

    /** Field description */
    private final String mesage;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ScmConfiguration configuration;

  /** Field description */
  private final SCMContextProvider contextProvider;

  /** Field description */
  private final AuthorizationCollector permissionCollector;

  /** Field description */
  private final RepositoryManager repositoryManger;

  /** Field description */
  private final SecuritySystem securitySystem;

  /** Field description */
  private final UserManager userManager;
}
