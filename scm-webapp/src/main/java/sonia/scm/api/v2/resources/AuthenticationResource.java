package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(AuthenticationResource.PATH)
public class AuthenticationResource {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationResource.class);

  static final String PATH = "v2/auth";

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
    @BeanParam AuthenticationRequestDto authentication
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
    AuthenticationRequestDto authentication
    ) {
    return authenticate(request, response, authentication);
  }

  private Response authenticate(
    HttpServletRequest request,
    HttpServletResponse response,
    AuthenticationRequestDto authentication
  ) {
    if (!authentication.isValid()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

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

      // TODO DisabledAccountException, ExcessiveAttemptsException for ui?

      return Response.status(Response.Status.UNAUTHORIZED).build();
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

}
