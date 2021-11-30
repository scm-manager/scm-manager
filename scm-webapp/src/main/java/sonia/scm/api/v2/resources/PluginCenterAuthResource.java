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
import sonia.scm.ExceptionWithContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.PluginCenterAuthenticator;
import sonia.scm.security.XsrfExcludes;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class PluginCenterAuthResource {

  @VisibleForTesting
  static final String ERROR_AUTHENTICATION_DISABLED = "8tSqFDot11";
  @VisibleForTesting
  static final String ERROR_ALREADY_AUTHENTICATED = "8XSqFEBd41";
  @VisibleForTesting
  static final String ERROR_CHALLENGE_MISSING = "FNSqFKQIR1";
  @VisibleForTesting
  static final String ERROR_CHALLENGE_DOES_NOT_MATCH = "8ESqFElpI1";

  private final PluginCenterAuthenticator authenticator;
  private final ScmConfiguration configuration;
  private final XsrfExcludes excludes;

  private String challenge;

  @Inject
  public PluginCenterAuthResource(PluginCenterAuthenticator authenticator, ScmConfiguration scmConfiguration, XsrfExcludes excludes) {
    this.authenticator = authenticator;
    this.configuration = scmConfiguration;
    this.excludes = excludes;
  }

  @GET
  @Path("")
  public Response auth(@Context HttpServletRequest request, @Context UriInfo uriInfo, @QueryParam("source") String sourceUri) {
    String pluginAuthUrl = configuration.getPluginAuthUrl();

    if (Strings.isNullOrEmpty(pluginAuthUrl)) {
      return error(request, ERROR_AUTHENTICATION_DISABLED);
    }

    if (authenticator.isAuthenticated()) {
      return error(request, ERROR_ALREADY_AUTHENTICATED);
    }

    challenge = UUID.randomUUID().toString();

    URI callbackUri = uriInfo.getAbsolutePathBuilder()
      .path("callback")
      .queryParam("source", sourceUri)
      .queryParam("challenge", challenge)
      .build();

    excludes.add(callbackUri.getPath());

    URI authUri = UriBuilder.fromUri(pluginAuthUrl).queryParam("instance", callbackUri.toASCIIString()).build();
    return Response.seeOther(authUri).build();
  }

  @POST
  @Path("callback")
  public Response callback(
    @Context HttpServletRequest request,
    @QueryParam("challenge") String challenge,
    @QueryParam("source") String source,
    @FormParam("refresh_token") String refreshToken
  ) {
    Optional<String> error = checkChallenge(challenge);
    if (error.isPresent()) {
      return error(request, error.get());
    }
    excludes.remove(request.getRequestURI());
    try {
      authenticator.authenticate(refreshToken);
    } catch (ExceptionWithContext ex) {
      return error(request, ex.getCode());
    }

    return redirect(request, source);
  }

  @GET
  @Path("callback")
  public Response callbackAbort(
    @Context HttpServletRequest request,
    @QueryParam("challenge") String challenge,
    @QueryParam("source") String source
  ) {
    Optional<String> error = checkChallenge(challenge);
    if (error.isPresent()) {
      return error(request, error.get());
    }
    excludes.remove(request.getRequestURI());
    return redirect(request, source);
  }

  private Response error(HttpServletRequest request, String code) {
    return redirect(HttpUtil.getCompleteUrl(request, "error", code));
  }

  private Response redirect(HttpServletRequest request, String source) {
    return redirect(HttpUtil.getCompleteUrl(request, source));
  }

  private Response redirect(String location) {
    return Response.status(Response.Status.SEE_OTHER).location(URI.create(location)).build();
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

}
