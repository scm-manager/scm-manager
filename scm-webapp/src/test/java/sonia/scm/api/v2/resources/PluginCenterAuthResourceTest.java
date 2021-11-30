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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.util.Providers;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.FetchAccessTokenFailedException;
import sonia.scm.plugin.PluginCenterAuthenticator;
import sonia.scm.security.XsrfExcludes;
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static sonia.scm.api.v2.resources.PluginCenterAuthResource.*;

@ExtendWith(MockitoExtension.class)
class PluginCenterAuthResourceTest {

  private final RestDispatcher dispatcher = new RestDispatcher();

  private final ScmConfiguration scmConfiguration = new ScmConfiguration();

  @Mock
  private PluginCenterAuthenticator authenticator;

  @Mock
  private XsrfExcludes excludes;

  @Mock
  private ChallengeGenerator challengeGenerator;

  @BeforeEach
  void setUpDispatcher() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(rootPathInfo);
    dispatcher.addSingletonResource(
      new PluginRootResource(
        null,
        null,
        null,
        Providers.of(new PluginCenterAuthResource(pathInfoStore, authenticator, scmConfiguration, excludes, challengeGenerator))
      )
    );
  }

  @Nested
  class AuthRequest {

    @Test
    void shouldReturnErrorRedirectWithoutSourceParameter() throws URISyntaxException {
      MockHttpResponse response = get("/v2/plugins/auth");
      assertError(response, ERROR_SOURCE_MISSING);
    }

    @Test
    void shouldReturnErrorRedirectWithoutPluginAuthUrlParameter() throws URISyntaxException {
      scmConfiguration.setPluginAuthUrl("");

      MockHttpResponse response = get("/v2/plugins/auth?source=/admin/plugins");
      assertError(response, ERROR_AUTHENTICATION_DISABLED);
    }

    @Test
    void shouldReturnErrorRedirectIfAlreadyAuthenticated() throws URISyntaxException {
      when(authenticator.isAuthenticated()).thenReturn(true);

      MockHttpResponse response = get("/v2/plugins/auth?source=/admin/plugins");
      assertError(response, ERROR_ALREADY_AUTHENTICATED);
    }

    @Test
    void shouldReturnRedirectToPluginAuthUrl() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("abcd");
      scmConfiguration.setPluginAuthUrl("https://plug.ins");

      MockHttpResponse response = get("/v2/plugins/auth?source=/admin/plugins");
      assertRedirect(response, "https://plug.ins?instance=%2Fv2%2Fplugins%2Fauth%2Fcallback?source%3D%2Fadmin%2Fplugins%26challenge%3Dabcd");
    }

    @Test
    void shouldExcludeCallbackFromXsrf() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("1234");
      scmConfiguration.setPluginAuthUrl("https://plug.ins");

      get("/v2/plugins/auth?source=/admin/plugins");

      verify(excludes).add("/v2/plugins/auth/callback");
    }

  }

  @Nested
  class AbortAuthentication {

    @Test
    void shouldReturnErrorRedirectWithoutChallengeParameter() throws URISyntaxException {
      MockHttpResponse response = get("/v2/plugins/auth/callback");
      assertError(response, ERROR_CHALLENGE_MISSING);
    }

    @Test
    void shouldReturnErrorRedirectWithChallengeMismatch() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      MockHttpResponse response = get("/v2/plugins/auth/callback?challenge=abc");
      assertError(response, ERROR_CHALLENGE_DOES_NOT_MATCH);
    }

    @Test
    void shouldRedirectToRoot() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      MockHttpResponse response = get("/v2/plugins/auth/callback?challenge=xyz");
      assertRedirect(response, "/");
    }

    @Test
    void shouldRedirectToSource() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      MockHttpResponse response = get("/v2/plugins/auth/callback?challenge=xyz&source=/repos");
      assertRedirect(response, "/repos");
    }

    @Test
    void shouldRemoveCallbackFromXsrf() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      get("/v2/plugins/auth/callback?challenge=xyz");
      verify(excludes).remove("/v2/plugins/auth/callback");
    }

  }

  @Nested
  class AuthenticationCallback {

    @Test
    void shouldReturnErrorRedirectWithoutChallengeParameter() throws URISyntaxException {
      MockHttpResponse response = post("/v2/plugins/auth/callback", "rf");
      assertError(response, ERROR_CHALLENGE_MISSING);
    }

    @Test
    void shouldReturnErrorRedirectWithChallengeMismatch() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      MockHttpResponse response = post("/v2/plugins/auth/callback?challenge=abc", "rf");
      assertError(response, ERROR_CHALLENGE_DOES_NOT_MATCH);
    }

    @Test
    void shouldReturnErrorRedirectFromFailedAuthentication() throws URISyntaxException {
      FetchAccessTokenFailedException exception = new FetchAccessTokenFailedException("failed ...");
      doThrow(exception).when(authenticator).authenticate("rf");
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      MockHttpResponse response = post("/v2/plugins/auth/callback?challenge=xyz", "rf");
      assertError(response, exception.getCode());
    }

    @Test
    void shouldAuthenticate() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      post("/v2/plugins/auth/callback?challenge=xyz", "refresh_token");
      verify(authenticator).authenticate("refresh_token");
    }

    @Test
    void shouldRedirectToSource() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/users");
      MockHttpResponse response = post("/v2/plugins/auth/callback?challenge=xyz&source=/users", "rrrrf");
      assertRedirect(response, "/users");
    }

    @Test
    void shouldRemoveCallbackFromXsrf() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      post("/v2/plugins/auth/callback?challenge=xyz", "rf");
      verify(excludes).remove("/v2/plugins/auth/callback");
    }

  }

  @CanIgnoreReturnValue
  private MockHttpResponse post(String uri, String refreshToken) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.post(uri);
    request.addFormHeader("refresh_token", refreshToken);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  @CanIgnoreReturnValue
  private MockHttpResponse get(String uri) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get(uri);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private void assertError(MockHttpResponse response, String code) {
    assertRedirect(response, "/error/" + code);
  }

  private void assertRedirect(MockHttpResponse response, String location) {
    assertRedirect(response, (locationHeader) -> assertThat(locationHeader).isEqualTo(location));
  }

  private void assertRedirect(MockHttpResponse response, Consumer<String> location) {
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_SEE_OTHER);
    location.accept(response.getOutputHeaders().getFirst("Location").toString());
  }

  private static final ScmPathInfo rootPathInfo = new ScmPathInfo() {
    @Override
    public URI getApiRestUri() {
      return URI.create("/api");
    }

    @Override
    public URI getRootUri() {
      return URI.create("/");
    }
  };
}
