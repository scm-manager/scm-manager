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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.subject.Subject;

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ScmState;
import sonia.scm.ScmStateFactory;
import sonia.scm.api.rest.RestActionResult;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.Tokens;
import sonia.scm.util.HttpUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.Scope;

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
   * @param configuration
   * @param stateFactory
   * @param tokenBuilderFactory
   * @param cookieIssuer
   */
  @Inject
  public AuthenticationResource(ScmConfiguration configuration,
    ScmStateFactory stateFactory, AccessTokenBuilderFactory tokenBuilderFactory, AccessTokenCookieIssuer cookieIssuer)
  {
    this.configuration = configuration;
    this.stateFactory = stateFactory;
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.cookieIssuer = cookieIssuer;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Authenticate a user and return the state of the application.<br />
   * <br />
   * <ul>
   *   <li>200 success</li>
   *   <li>400 bad request, required parameter is missing.</li>
   *   <li>401 unauthorized, the specified username or password is wrong</li>
   *   <li>500 internal server error</li>
   * </ul>
   *
   * @param request current http request
   * @param response current http response
   * @param username the username for the authentication
   * @param password the password for the authentication
   * @param cookie create authentication token
   * @param scope scope of created token
   *
   * @return
   */
  @POST
  @Path("login")
  @TypeHint(ScmState.class)
  public Response authenticate(@Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @FormParam("username") String username,
    @FormParam("password") String password, 
    @QueryParam("cookie") boolean cookie,
    @QueryParam("scope") List<String> scope)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(username),
      "username parameter is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(password),
      "password parameter is required");

    Response res;
    Subject subject = SecurityUtils.getSubject();

    try
    {
      subject.login(Tokens.createAuthenticationToken(request, username, password));      
      AccessTokenBuilder tokenBuilder = tokenBuilderFactory.create();
      if ( scope != null ) {
        tokenBuilder.scope(Scope.valueOf(scope));
      }
      AccessToken token = tokenBuilder.build();

      ScmState state;

      if (cookie) {
        cookieIssuer.authenticate(request, response, token);
        state = stateFactory.createState(subject);
      } else {
        state = stateFactory.createState(subject, token.compact());
      }

      res = Response.ok(state).build();
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

      res = handleFailedAuthentication(request, ex, Response.Status.FORBIDDEN,
        WUIAuthenticationFailure.LOCKED);
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

      res = handleFailedAuthentication(request, ex, Response.Status.FORBIDDEN,
        WUIAuthenticationFailure.TEMPORARY_LOCKED);
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

      res = handleFailedAuthentication(request, ex,
        Response.Status.UNAUTHORIZED,
        WUIAuthenticationFailure.WRONG_CREDENTIALS);
    }

    return res;
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
  public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response)
  {
    Subject subject = SecurityUtils.getSubject();

    subject.logout();

    // remove authentication cookie
    cookieIssuer.invalidate(request, response);

    Response resp;

    if (configuration.isAnonymousAccessEnabled())
    {
      resp = Response.ok(stateFactory.createAnonymousState()).build();
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

      ScmState state = stateFactory.createState(subject);

      response = Response.ok(state).build();
    }
    else if (configuration.isAnonymousAccessEnabled())
    {

      response = Response.ok(stateFactory.createAnonymousState()).build();
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
  private final ScmStateFactory stateFactory;

  /** Field description */
  private final AccessTokenBuilderFactory tokenBuilderFactory; 
  
  /** Field description */
  private final AccessTokenCookieIssuer cookieIssuer;
}
