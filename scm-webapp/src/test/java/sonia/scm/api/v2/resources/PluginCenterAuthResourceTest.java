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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.util.Providers;
import lombok.Value;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.AuthenticationInfo;
import sonia.scm.plugin.FetchAccessTokenFailedException;
import sonia.scm.plugin.PluginCenterAuthenticator;
import sonia.scm.security.XsrfExcludes;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.user.UserTestData;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static sonia.scm.api.v2.resources.PluginCenterAuthResource.*;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class PluginCenterAuthResourceTest {

  private final RestDispatcher dispatcher = new RestDispatcher();

  private final ScmConfiguration scmConfiguration = new ScmConfiguration();

  @Mock
  private PluginCenterAuthenticator authenticator;

  @Mock
  private XsrfExcludes excludes;

  @Mock
  private ChallengeGenerator challengeGenerator;

  @Mock
  private UserDisplayManager userDisplayManager;

  @BeforeEach
  void setUpDispatcher() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(rootPathInfo);
    dispatcher.addSingletonResource(
      new PluginRootResource(
        null,
        null,
        null,
        Providers.of(new PluginCenterAuthResource(pathInfoStore, authenticator, userDisplayManager, scmConfiguration, excludes, challengeGenerator))
      )
    );
  }

  @Nested
  class GetAuthenticationInfo {

    @Test
    void shouldReturnNotFoundIfNotAuthenticated() throws URISyntaxException {
      MockHttpResponse response = get("/v2/plugins/auth", VndMediaType.PLUGIN_CENTER_AUTH_INFO);
      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void shouldReturnAuthenticationInfo() throws IOException, URISyntaxException {
      JsonNode root = requestAuthInfo();

      assertThat(root.get("principal").asText()).isEqualTo("Tricia McMillan");
      assertThat(root.get("pluginCenterSubject").asText()).isEqualTo("tricia.mcmillan@hitchhiker.com");
      assertThat(root.get("date").asText()).isNotEmpty();
      assertThat(root.get("_links").get("self").get("href").asText()).isEqualTo("/v2/plugins/auth");
    }

    @Test
    void shouldNotReturnLogoutLinkWithoutWritePermission() throws IOException, URISyntaxException {
      JsonNode root = requestAuthInfo();

      assertThat(root.get("_links").has("logout")).isFalse();
    }

    @Test
    @SubjectAware(value = "marvin", permissions = "plugin:write")
    void shouldNotReturnLogoutLinkIfPermitted() throws IOException, URISyntaxException {
      JsonNode root = requestAuthInfo();

      assertThat(root.get("_links").get("logout").get("href").asText()).isEqualTo("/v2/plugins/auth");
    }

    private JsonNode requestAuthInfo() throws IOException, URISyntaxException {
      AuthenticationInfo info = new SimpleAuthenticationInfo(
        "trillian", "tricia.mcmillan@hitchhiker.com", Instant.now()
      );
      when(authenticator.getAuthenticationInfo()).thenReturn(Optional.of(info));

      DisplayUser user = DisplayUser.from(UserTestData.createTrillian());
      when(userDisplayManager.get("trillian")).thenReturn(Optional.of(user));

      MockHttpResponse response = get("/v2/plugins/auth", VndMediaType.PLUGIN_CENTER_AUTH_INFO);

      ObjectMapper mapper = new ObjectMapper();
      return mapper.readTree(response.getContentAsString());
    }

  }

  @Nested
  class Logout {

    @Test
    void shouldLogout() throws URISyntaxException {
      MockHttpResponse response = request(MockHttpRequest.delete("/v2/plugins/auth"));

      verify(authenticator).logout();

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    }

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
      MockHttpResponse response = post("/v2/plugins/auth/callback", "trillian", "rf");
      assertError(response, ERROR_CHALLENGE_MISSING);
    }

    @Test
    void shouldReturnErrorRedirectWithChallengeMismatch() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      MockHttpResponse response = post("/v2/plugins/auth/callback?challenge=abc", "trillian", "rf");
      assertError(response, ERROR_CHALLENGE_DOES_NOT_MATCH);
    }

    @Test
    void shouldReturnErrorRedirectFromFailedAuthentication() throws URISyntaxException {
      FetchAccessTokenFailedException exception = new FetchAccessTokenFailedException("failed ...");
      doThrow(exception).when(authenticator).authenticate("trillian", "rf");
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      MockHttpResponse response = post("/v2/plugins/auth/callback?challenge=xyz", "trillian", "rf");
      assertError(response, exception.getCode());
    }

    @Test
    void shouldAuthenticate() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      post("/v2/plugins/auth/callback?challenge=xyz", "trillian", "refresh_token");
      verify(authenticator).authenticate("trillian", "refresh_token");
    }

    @Test
    void shouldRedirectToSource() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/users");
      MockHttpResponse response = post("/v2/plugins/auth/callback?challenge=xyz&source=/users", "tricia", "rrrrf");
      assertRedirect(response, "/users");
    }

    @Test
    void shouldRemoveCallbackFromXsrf() throws URISyntaxException {
      when(challengeGenerator.create()).thenReturn("xyz");
      get("/v2/plugins/auth?source=/repos");
      post("/v2/plugins/auth/callback?challenge=xyz", "trillian", "rf");
      verify(excludes).remove("/v2/plugins/auth/callback");
    }

  }

  @CanIgnoreReturnValue
  private MockHttpResponse post(String uri, String subject, String refreshToken) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.post(uri);
    request.addFormHeader("subject", subject);
    request.addFormHeader("refresh_token", refreshToken);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  @CanIgnoreReturnValue
  private MockHttpResponse get(String uri) throws URISyntaxException {
    return get(uri, null);
  }

  @CanIgnoreReturnValue
  private MockHttpResponse get(String uri, String accept) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get(uri);
    if (accept != null) {
      request.accept(accept);
    }
    return request(request);
  }

  private MockHttpResponse request(MockHttpRequest request) {
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

  @Value
  private static class SimpleAuthenticationInfo implements AuthenticationInfo {
    String principal;
    String pluginCenterSubject;
    Instant date;
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
