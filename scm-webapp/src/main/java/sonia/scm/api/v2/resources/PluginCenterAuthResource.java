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

import lombok.Data;
import sonia.scm.security.XsrfExcludes;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.UUID;

public class PluginCenterAuthResource {

  private final ConfigurationStore<Configuration> configurationStore;
  private final XsrfExcludes excludes;

  @Inject
  public PluginCenterAuthResource(ConfigurationStoreFactory configurationStoreFactory, XsrfExcludes excludes) {
    this.configurationStore = configurationStoreFactory.withType(Configuration.class).withName("plugin-center-auth").build();
    this.excludes = excludes;
  }

  @GET
  @Path("")
  public Response auth(@Context UriInfo uriInfo, @QueryParam("source") String sourceUri) {
    // TODO check if already authenticated

    String challenge = UUID.randomUUID().toString();

    URI callbackUri = uriInfo.getAbsolutePathBuilder()
      .path("callback")
      .queryParam("source", sourceUri)
      .queryParam("challenge", challenge)
      .build();

    excludes.add(callbackUri.getPath());

    Configuration configuration = new Configuration();
    configuration.setChallenge(challenge);
    configurationStore.set(configuration);

    // TODO read from scm configuration
    URI authUri = URI.create("http://localhost:8000/api/v1/auth/oidc?instance=" + HttpUtil.encode(callbackUri.toASCIIString()));
    return Response.temporaryRedirect(authUri).build();
  }

  @GET
  @Path("callback")
  public Response callbackAbort(@Context HttpServletRequest request, @QueryParam("challenge") String challenge, @QueryParam("source") String source) {
    checkChallenge(challenge);

    excludes.remove(request.getRequestURI());
    return redirect(request, source);
  }

  private Response redirect(HttpServletRequest request, String source) {
    String completeUrl = HttpUtil.getCompleteUrl(request, source);

    return Response.status(Response.Status.SEE_OTHER).location(URI.create(completeUrl)).build();
  }

  private void checkChallenge(String challenge) {
    Configuration configuration = configurationStore.getOptional().orElse(new Configuration());
    if (!challenge.equals(configuration.getChallenge())) {
      // TODO better exception
      throw new IllegalArgumentException("challenge does not match");
    }
  }

  @POST
  @Path("callback")
  public Response callback(@Context HttpServletRequest request, @QueryParam("challenge") String challenge, @QueryParam("source") String source, @FormParam("refresh_token") String refreshToken) {
    checkChallenge(challenge);

    excludes.remove(request.getRequestURI());

    Configuration configuration = new Configuration();
    configuration.setRefreshToken(refreshToken);
    configurationStore.set(configuration);

    return redirect(request, source);
  }

  @Data
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Configuration {

    private String challenge;
    private String refreshToken;

  }

}
