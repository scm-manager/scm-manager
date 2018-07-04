package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.rest.RestActionResult;
import sonia.scm.security.*;
import sonia.scm.util.HttpUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by masuewer on 04.07.18.
 */
@Path(AuthenticationResource.PATH)
public class AuthenticationResource {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationResource.class);

  public static final String PATH = "v2/auth";

  private final AccessTokenBuilderFactory tokenBuilderFactory;
  private final AccessTokenCookieIssuer cookieIssuer;

  @Inject
  public AuthenticationResource(AccessTokenBuilderFactory tokenBuilderFactory, AccessTokenCookieIssuer cookieIssuer)
  {
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.cookieIssuer = cookieIssuer;
  }


  @POST
  @Path("access_token")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, required parameter is missing"),
    @ResponseCode(code = 401, condition = "unauthorized, the specified username or password is wrong"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response authenticateViaForm(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @BeanParam AuthenticationRequest authentication
  ) {
    return authenticate(request, response, authentication);
  }

  @POST
  @Path("access_token")
  @Consumes(MediaType.APPLICATION_JSON)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "bad request, required parameter is missing"),
    @ResponseCode(code = 401, condition = "unauthorized, the specified username or password is wrong"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response authenticateViaJSONBody(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    AuthenticationRequest authentication
    ) {
    return authenticate(request, response, authentication);
  }

  private Response authenticate(
    HttpServletRequest request,
    HttpServletResponse response,
    AuthenticationRequest authentication
  ) {
    authentication.validate();

    Response res;
    Subject subject = SecurityUtils.getSubject();

    try
    {
      subject.login(Tokens.createAuthenticationToken(request, authentication.getUsername(), authentication.getPassword()));

      AccessTokenBuilder tokenBuilder = tokenBuilderFactory.create();
      if ( authentication.getScope() != null ) {
        tokenBuilder.scope(Scope.valueOf(authentication.getScope()));
      }

      AccessToken token = tokenBuilder.build();

      if (authentication.isCookie()) {
        cookieIssuer.authenticate(request, response, token);
        res = Response.noContent().build();
      } else {
        res = Response.ok( token.compact() ).build();
      }
    }
    catch (DisabledAccountException ex)
    {
      if (LOG.isTraceEnabled())
      {
        LOG.trace(
          "authentication failed, account user ".concat(authentication.getUsername()).concat(
            " is locked"), ex);
      }
      else
      {
        LOG.warn("authentication failed, account {} is locked", authentication.getUsername());
      }

      res = handleFailedAuthentication(request, ex, Response.Status.FORBIDDEN,
        WUIAuthenticationFailure.LOCKED);
    }
    catch (ExcessiveAttemptsException ex)
    {
      if (LOG.isTraceEnabled())
      {
        LOG.trace(
          "authentication failed, account user ".concat(authentication.getUsername()).concat(
            " is temporary locked"), ex);
      }
      else
      {
        LOG.warn("authentication failed, account {} is temporary locked", authentication.getUsername());
      }

      res = handleFailedAuthentication(request, ex, Response.Status.FORBIDDEN,
        WUIAuthenticationFailure.TEMPORARY_LOCKED);
    }
    catch (AuthenticationException ex)
    {
      if (LOG.isTraceEnabled())
      {
        LOG.trace("authentication failed for user ".concat(authentication.getUsername()), ex);
      }
      else
      {
        LOG.warn("authentication failed for user {}", authentication.getUsername());
      }

      res = handleFailedAuthentication(request, ex, Response.Status.UNAUTHORIZED,
        WUIAuthenticationFailure.WRONG_CREDENTIALS);
    }

    return res;
  }

  @DELETE
  @Path("access_token")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response)
  {
    Subject subject = SecurityUtils.getSubject();

    subject.logout();

    // remove authentication cookie
    cookieIssuer.invalidate(request, response);

    // TODO anonymous access ??
    return Response.noContent().build();
  }

  public static class AuthenticationRequest {

    @FormParam("grant_type")
    @JsonProperty("grant_type")
    private String grantType;

    @FormParam("username")
    private String username;

    @FormParam("password")
    private String password;

    @FormParam("cookie")
    private boolean cookie;

    @FormParam("scope")
    private List<String> scope;

    public String getGrantType() {
      return grantType;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }

    public boolean isCookie() {
      return cookie;
    }

    public List<String> getScope() {
      return scope;
    }

    public void validate() {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(grantType), "grant_type parameter is required");
      Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "username parameter is required");
      Preconditions.checkArgument(!Strings.isNullOrEmpty(password), "password parameter is required");
    }
  }


  private Response handleFailedAuthentication(HttpServletRequest request,
                                              AuthenticationException ex, Response.Status status,
                                              WUIAuthenticationFailure failure) {
    Response response;

    if (HttpUtil.isWUIRequest(request)) {
      response = Response.ok(new WUIAuthenticationFailedResult(failure,
        ex.getMessage())).build();
    } else {
      response = Response.status(status).build();
    }

    return response;
  }

  private enum WUIAuthenticationFailure { LOCKED, TEMPORARY_LOCKED, WRONG_CREDENTIALS }

  @XmlRootElement(name = "result")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static final class WUIAuthenticationFailedResult extends RestActionResult {

    private final WUIAuthenticationFailure failure;
    private final String message;

    public WUIAuthenticationFailedResult(WUIAuthenticationFailure failure, String message) {
      super(false);
      this.failure = failure;
      this.message = message;
    }

    public WUIAuthenticationFailure getFailure() {
      return failure;
    }

    public String getMessage() {
      return message;
    }

  }

}
