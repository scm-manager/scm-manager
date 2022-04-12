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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import sonia.scm.ExceptionWithContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.AuthenticationInfo;
import sonia.scm.plugin.PluginCenterAuthenticator;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.security.Impersonator;
import sonia.scm.security.SecureParameterSerializer;
import sonia.scm.security.XsrfExcludes;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class PluginCenterAuthResource {

  @VisibleForTesting
  static final String ERROR_SOURCE_MISSING = "5DSqG6Mcg1";
  @VisibleForTesting
  static final String ERROR_AUTHENTICATION_DISABLED = "8tSqFDot11";
  @VisibleForTesting
  static final String ERROR_ALREADY_AUTHENTICATED = "8XSqFEBd41";
  @VisibleForTesting
  static final String ERROR_PARAMS_MISSING = "52SqQBdpO1";
  @VisibleForTesting
  static final String ERROR_CHALLENGE_MISSING = "FNSqFKQIR1";
  @VisibleForTesting
  static final String ERROR_CHALLENGE_DOES_NOT_MATCH = "8ESqFElpI1";

  private static final String METHOD_LOGIN = "login";
  private static final String METHOD_LOGOUT = "logout";

  private final ScmPathInfoStore pathInfoStore;
  private final PluginCenterAuthenticator authenticator;
  private final ScmConfiguration configuration;
  private final UserDisplayManager userDisplayManager;
  private final XsrfExcludes excludes;
  private final ChallengeGenerator challengeGenerator;
  private final SecureParameterSerializer parameterSerializer;
  private final Impersonator impersonator;

  private String challenge;

  @Inject
  public PluginCenterAuthResource(
    ScmPathInfoStore pathInfoStore,
    PluginCenterAuthenticator authenticator,
    UserDisplayManager userDisplayManager,
    ScmConfiguration scmConfiguration,
    XsrfExcludes excludes,
    SecureParameterSerializer parameterSerializer,
    Impersonator impersonator) {
    this(
      pathInfoStore, authenticator, userDisplayManager, scmConfiguration, excludes, () -> UUID.randomUUID().toString(),
      parameterSerializer, impersonator);
  }

  @VisibleForTesting
  @SuppressWarnings("java:S107") // parameter count is ok for testing
  PluginCenterAuthResource(
    ScmPathInfoStore pathInfoStore,
    PluginCenterAuthenticator authenticator,
    UserDisplayManager userDisplayManager,
    ScmConfiguration configuration,
    XsrfExcludes excludes,
    ChallengeGenerator challengeGenerator,
    SecureParameterSerializer parameterSerializer,
    Impersonator impersonator) {
    this.pathInfoStore = pathInfoStore;
    this.authenticator = authenticator;
    this.configuration = configuration;
    this.userDisplayManager = userDisplayManager;
    this.excludes = excludes;
    this.challengeGenerator = challengeGenerator;
    this.parameterSerializer = parameterSerializer;
    this.impersonator = impersonator;
  }

  @GET
  @Path("")
  @Operation(
    summary = "Return plugin center auth info",
    description = "Return authentication information of plugin center connection",
    tags = "Plugin Management",
    operationId = "plugin_center_auth_information"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.PLUGIN_COLLECTION,
      schema = @Schema(implementation = PluginCenterAuthenticationInfoDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:read\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @Produces(VndMediaType.PLUGIN_CENTER_AUTH_INFO)
  public Response authenticationInfo(@Context UriInfo uriInfo) {
    Optional<AuthenticationInfo> authentication = authenticator.getAuthenticationInfo();
    if (authentication.isPresent()) {
      return Response.ok(createAuthenticatedDto(uriInfo, authentication.get())).build();
    }
    PluginCenterAuthenticationInfoDto dto = new PluginCenterAuthenticationInfoDto(createLinks(uriInfo, null));
    dto.setDefault(configuration.isDefaultPluginAuthUrl());
    return Response.ok(dto).build();
  }

  private PluginCenterAuthenticationInfoDto createAuthenticatedDto(UriInfo uriInfo, AuthenticationInfo info) {
    PluginCenterAuthenticationInfoDto dto = new PluginCenterAuthenticationInfoDto(
      createLinks(uriInfo, info)
    );

    dto.setPrincipal(getPrincipalDisplayName(info.getPrincipal()));
    dto.setPluginCenterSubject(info.getPluginCenterSubject());
    dto.setDate(info.getDate());
    dto.setDefault(configuration.isDefaultPluginAuthUrl());
    dto.setFailed(info.isFailed());
    return dto;
  }

  @GET
  @Path("login")
  @Operation(
    summary = "Login",
    description = "Start the authentication flow to connect the plugin center with an account",
    tags = "Plugin Management",
    operationId = "plugin_center_auth_login"
  )
  @ApiResponse(
    responseCode = "303",
    description = "See other"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:write\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response login(
    @Context UriInfo uriInfo, @QueryParam("source") String source, @QueryParam("reconnect") boolean reconnect
  ) throws IOException {
    String pluginAuthUrl = configuration.getPluginAuthUrl();

    if (Strings.isNullOrEmpty(source)) {
      return error(ERROR_SOURCE_MISSING);
    }

    if (Strings.isNullOrEmpty(pluginAuthUrl)) {
      return error(ERROR_AUTHENTICATION_DISABLED);
    }

    if (!reconnect && authenticator.isAuthenticated()) {
      return error(ERROR_ALREADY_AUTHENTICATED);
    }

    challenge = challengeGenerator.create();

    URI selfUri = uriInfo.getAbsolutePath();
    selfUri = selfUri.resolve(selfUri.getPath().replace("/login", "/callback"));

    String principal = SecurityUtils.getSubject().getPrincipal().toString();

    AuthParameter parameter = new AuthParameter(
      principal,
      challenge,
      source
    );

    URI callbackUri = UriBuilder.fromUri(selfUri)
      .queryParam("params", parameterSerializer.serialize(parameter))
      .build();

    excludes.add(callbackUri.getPath());

    URI authUri = UriBuilder.fromUri(pluginAuthUrl).queryParam("instance", callbackUri.toASCIIString()).build();
    return Response.seeOther(authUri).build();
  }

  private Links createLinks(UriInfo uriInfo, @Nullable AuthenticationInfo info) {
    String self = uriInfo.getAbsolutePath().toASCIIString();
    Links.Builder builder = Links.linkingTo().self(self);
    if (PluginPermissions.write().isPermitted()) {
      if (info != null) {
        builder.single(Link.link(METHOD_LOGOUT, self));
        if (info.isFailed()) {
          String reconnectLink = uriInfo.getAbsolutePathBuilder()
            .path(METHOD_LOGIN)
            .queryParam("reconnect", "true")
            .build()
            .toASCIIString();
          builder.single(Link.link("reconnect", reconnectLink));
        }
      } else if (!Strings.isNullOrEmpty(configuration.getPluginAuthUrl())) {
        builder.single(Link.link(METHOD_LOGIN, uriInfo.getAbsolutePathBuilder().path(METHOD_LOGIN).build().toASCIIString()));
      }
    }
    return builder.build();
  }

  private String getPrincipalDisplayName(String principal) {
    return userDisplayManager.get(principal).map(DisplayUser::getDisplayName).orElse(principal);
  }

  @DELETE
  @Path("")
  @Operation(
    summary = "Logout",
    description = "Start the authentication flow to connect the plugin center with an account",
    tags = "Plugin Management",
    operationId = "plugin_center_auth_logout"
  )
  @ApiResponse(
    responseCode = "204",
    description = "No content"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:write\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response logout() {
    authenticator.logout();
    return Response.noContent().build();
  }

  @POST
  @Path("callback")
  @Operation(
    summary = "Finalize authentication",
    description = "Callback endpoint for the authentication flow to finalize the authentication",
    tags = "Plugin Management",
    operationId = "plugin_center_auth_callback"
  )
  @ApiResponse(
    responseCode = "303",
    description = "See other"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:write\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @AllowAnonymousAccess
  public Response callback(
    @Context UriInfo uriInfo,
    @QueryParam("params") String encryptedParams,
    @FormParam("subject") String subject,
    @FormParam("refresh_token") String refreshToken
  ) throws IOException {
    if (Strings.isNullOrEmpty(encryptedParams)) {
      return error(ERROR_PARAMS_MISSING);
    }

    AuthParameter params = parameterSerializer.deserialize(encryptedParams, AuthParameter.class);

    Optional<String> error = checkChallenge(params.getChallenge());
    if (error.isPresent()) {
      return error(error.get());
    }

    challenge = null;
    excludes.remove(uriInfo.getPath());

    PrincipalCollection principal = createPrincipalCollection(params);
    try (Impersonator.Session session = impersonator.impersonate(principal)) {
      authenticator.authenticate(subject, refreshToken);
    } catch (ExceptionWithContext ex) {
      return error(ex.getCode());
    }

    return redirect(params.getSource());
  }

  private PrincipalCollection createPrincipalCollection(AuthParameter params) {
    SimplePrincipalCollection principal = new SimplePrincipalCollection(
      params.getPrincipal(), "pluginCenterAuth"
    );
    User user = new User(params.getPrincipal());
    principal.add(user, "pluginCenterAuth");
    return principal;
  }

  @GET
  @Path("callback")
  @Operation(
    summary = "Abort authentication",
    description = "Callback endpoint for the authentication flow to abort the authentication",
    tags = "Plugin Management",
    operationId = "plugin_center_auth_callback_abort"
  )
  @ApiResponse(
    responseCode = "303",
    description = "See other"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"plugin:write\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response callbackAbort(@Context UriInfo uriInfo, @QueryParam("params") String encryptedParams) throws IOException {
    if (Strings.isNullOrEmpty(encryptedParams)) {
      return error(ERROR_PARAMS_MISSING);
    }

    AuthParameter params = parameterSerializer.deserialize(encryptedParams, AuthParameter.class);

    Optional<String> error = checkChallenge(params.getChallenge());
    if (error.isPresent()) {
      return error(error.get());
    }

    challenge = null;

    excludes.remove(uriInfo.getPath());

    return redirect(params.getSource());
  }

  private Response error(String code) {
    return redirect("error/" + code);
  }

  private Response redirect(String location) {
    URI rootUri = pathInfoStore.get().getRootUri();
    String path = rootUri.getPath();
    if (!Strings.isNullOrEmpty(location)) {
      path = HttpUtil.concatenate(path, location);
    }
    return redirect(rootUri.resolve(path));
  }

  private Response redirect(URI location) {
    return Response.status(Response.Status.SEE_OTHER).location(location).build();
  }

  private Optional<String> checkChallenge(String challengeFromRequest) {
    if (Strings.isNullOrEmpty(challenge)) {
      return Optional.of(ERROR_CHALLENGE_MISSING);
    }
    if (!challenge.equals(challengeFromRequest)) {
      return Optional.of(ERROR_CHALLENGE_DOES_NOT_MATCH);
    }
    return Optional.empty();
  }

  @FunctionalInterface
  interface ChallengeGenerator {
    String create();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class AuthParameter {

    private String principal;
    private String challenge;
    private String source;

  }

}
