/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.metrics.AuthenticationMetrics;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.security.LogoutEvent;
import sonia.scm.security.Scope;
import sonia.scm.security.Tokens;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.util.Optional;

@SecuritySchemes({
  @SecurityScheme(
    name = "Basic_Authentication",
    description = "HTTP Basic authentication with username and password",
    scheme = "basic",
    type = SecuritySchemeType.HTTP
  ),
  @SecurityScheme(
    name = "Bearer_Token_Authentication",
    description = "Authentication with a jwt bearer token",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP
  )
})
@OpenAPIDefinition(tags = {
  @Tag(name = "Authentication", description = "Authentication related endpoints")
})
@Path(AuthenticationResource.PATH)
public class AuthenticationResource {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationResource.class);

  static final String PATH = "v2/auth";
  private static final String AUTH_METRIC_TYPE = "UI/REST";

  private final AccessTokenBuilderFactory tokenBuilderFactory;
  private final AccessTokenCookieIssuer cookieIssuer;
  private final Counter loginAttemptsCounter;
  private final Counter loginFailedCounter;
  private final Counter logoutCounter;
  private final ScmEventBus eventBus;

  @Inject(optional = true)
  private LogoutRedirection logoutRedirection;

  @Inject
  public AuthenticationResource(AccessTokenBuilderFactory tokenBuilderFactory, AccessTokenCookieIssuer cookieIssuer, MeterRegistry meterRegistry, ScmEventBus eventBus) {
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.cookieIssuer = cookieIssuer;
    this.loginAttemptsCounter = AuthenticationMetrics.loginAttempts(meterRegistry, AUTH_METRIC_TYPE);
    this.loginFailedCounter = AuthenticationMetrics.loginFailed(meterRegistry, AUTH_METRIC_TYPE);
    this.logoutCounter = AuthenticationMetrics.logout(meterRegistry, AUTH_METRIC_TYPE);
    this.eventBus = eventBus;
  }

  @POST
  @Path("access_token")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Operation(
    summary = "Login via Form",
    description = "Form-based authentication.",
    tags = "Authentication",
    hidden = true
  )
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "204", description = "success without content")
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
  @AllowAnonymousAccess
  public Response authenticateViaForm(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @BeanParam AuthenticationRequestDto authentication
  ) {
    return authenticate(request, response, authentication);
  }

  @POST
  @Path("access_token")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Login via JSON",
    description = "JSON-based authentication.",
    tags = "Authentication",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = MediaType.APPLICATION_JSON,
        schema = @Schema(implementation = AuthenticationRequestDto.class),
        examples = @ExampleObject(
          name = "Simple login",
          value = "{\n  \"username\":\"scmadmin\",\n  \"password\":\"scmadmin\",\n  \"cookie\":false,\n  \"grant_type\":\"password\"\n}",
          summary = "Authenticate with username and password"
        )
      )
    )
  )
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "204", description = "success without content")
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
  @AllowAnonymousAccess
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
    loginAttemptsCounter.increment();

    if (!authentication.isValid()) {
      loginFailedCounter.increment();
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
      loginFailedCounter.increment();
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
    logoutCounter.increment();

    Subject subject = SecurityUtils.getSubject();
    String primaryPrincipal = subject.getPrincipals().getPrimaryPrincipal().toString();
    subject.logout();

    eventBus.post(new LogoutEvent(primaryPrincipal));

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
