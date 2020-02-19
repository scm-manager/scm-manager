package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.security.Scope;
import sonia.scm.security.Tokens;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

@SecuritySchemes({
  @SecurityScheme(
    name = "Basic Authentication",
    description = "HTTP Basic authentication with username and password",
    scheme = "Basic",
    type = SecuritySchemeType.HTTP
  ),
  @SecurityScheme(
    name = "Bearer Token Authentication",
    in = SecuritySchemeIn.HEADER,
    paramName = "Authorization",
    scheme = "Bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.APIKEY
  )
})
@OpenAPIDefinition(tags = {
  @Tag(name = "Authentication", description = "Authentication related endpoints")
})
@Path(AuthenticationResource.PATH)
@AllowAnonymousAccess
public class AuthenticationResource {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationResource.class);

  static final String PATH = "v2/auth";

  private final AccessTokenBuilderFactory tokenBuilderFactory;
  private final AccessTokenCookieIssuer cookieIssuer;

  @Inject(optional = true)
  private LogoutRedirection logoutRedirection;

  @Inject
  public AuthenticationResource(AccessTokenBuilderFactory tokenBuilderFactory, AccessTokenCookieIssuer cookieIssuer) {
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.cookieIssuer = cookieIssuer;
  }

  @POST
  @Path("access_token")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Operation(summary = "Login via Form", description = "Form-based authentication.", tags = "Authentication")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "400", description = "bad request, required parameter is missing")
  @ApiResponse(responseCode = "401", description = "unauthorized, the specified username or password is wrong")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
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
  @Operation(summary = "Login via JSON", description = "JSON-based authentication.", tags = "Authentication")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "400", description = "bad request, required parameter is missing")
  @ApiResponse(responseCode = "401", description = "unauthorized, the specified username or password is wrong")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
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

    try {
      subject.login(Tokens.createAuthenticationToken(request, authentication.getUsername(), authentication.getPassword()));

      AccessTokenBuilder tokenBuilder = tokenBuilderFactory.create();
      if (authentication.getScope() != null) {
        tokenBuilder.scope(Scope.valueOf(authentication.getScope()));
      }

      AccessToken token = tokenBuilder.build();

      if (authentication.isCookie()) {
        cookieIssuer.authenticate(request, response, token);
        res = Response.noContent().build();
      } else {
        res = Response.ok(token.compact()).build();
      }
    } catch (AuthenticationException ex) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("authentication failed for user ".concat(authentication.getUsername()), ex);
      } else {
        LOG.warn("authentication failed for user {}", authentication.getUsername());
      }

      // TODO DisabledAccountException, ExcessiveAttemptsException for ui?

      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    return res;
  }

  @DELETE
  @Path("access_token")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Logout", description = "Removes the access token.", tags = "Authentication")
  @ApiResponse(responseCode = "204", description = "success")
  @ApiResponse(responseCode = "500", description = "internal server error")
  public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
    Subject subject = SecurityUtils.getSubject();

    subject.logout();

    // remove authentication cookie
    cookieIssuer.invalidate(request, response);

    if (logoutRedirection == null) {
      return Response.noContent().build();
    } else {
      Optional<URI> uri = logoutRedirection.afterLogoutRedirectTo();
      if (uri.isPresent()) {
        return Response.ok(new RedirectAfterLogoutDto(uri.get().toASCIIString())).build();
      } else {
        return Response.noContent().build();
      }
    }
  }

  void setLogoutRedirection(LogoutRedirection logoutRedirection) {
    this.logoutRedirection = logoutRedirection;
  }
}
