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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.AuthenticationInfo;
import sonia.scm.plugin.FetchAccessTokenFailedException;
import sonia.scm.plugin.PluginCenterAuthenticator;
import sonia.scm.security.Impersonator;
import sonia.scm.security.SecureParameterSerializer;
import sonia.scm.security.XsrfExcludes;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.user.UserTestData;
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

  @Mock
  private SecureParameterSerializer parameterSerializer;

  @Mock
  private Impersonator impersonator;

  @BeforeEach
  void setUpDispatcher() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(rootPathInfo);

    PluginCenterAuthResource resource = new PluginCenterAuthResource(
      pathInfoStore, authenticator, userDisplayManager,
      scmConfiguration, excludes, challengeGenerator,
      parameterSerializer, impersonator
    );

    dispatcher.addSingletonResource(
      new PluginRootResource(
        null,
        null,
        null,
        Providers.of(resource)
      )
    );
  }

  @Nested
  class GetAuthenticationInfo {

    @Test
    void shouldReturnEmptyAuthenticationInfo() throws URISyntaxException, IOException {
      JsonNode root = getJson("/v2/plugins/auth");

      assertThat(root.has("principal")).isFalse();
      assertThat(root.has("pluginCenterSubject")).isFalse();
      assertThat(root.has("date")).isFalse();
      assertThat(root.get("_links").get("self").get("href").asText()).isEqualTo("/v2/plugins/auth");
    }

    @Test
    void shouldReturnTrueForIsDefault() throws URISyntaxException, IOException {
      JsonNode root = getJson("/v2/plugins/auth");

      assertThat(root.get("default").asBoolean()).isTrue();
    }

    @Test
    void shouldReturnFalseIfTheAuthUrlIsNotDefault() throws URISyntaxException, IOException {
      scmConfiguration.setPluginAuthUrl("https://plug.ins");

      JsonNode root = getJson("/v2/plugins/auth");

      assertThat(root.get("default").asBoolean()).isFalse();
    }

    @Test
    @SubjectAware(value = "marvin", permissions = "plugin:write")
    void shouldReturnLoginLinkIfPermitted() throws URISyntaxException, IOException {
      JsonNode root = getJson("/v2/plugins/auth");

      assertThat(root.get("_links").get("login").get("href").asText()).isEqualTo("/v2/plugins/auth/login");
    }

    @Test
    @SubjectAware(value = "marvin", permissions = "plugin:write")
    void shouldNotReturnLoginLinkIfPermittedButNotConfigured() throws URISyntaxException, IOException {
      scmConfiguration.setPluginAuthUrl(null);

      JsonNode root = getJson("/v2/plugins/auth");

      assertThat(root.get("_links").get("login")).isNull();
    }

    @Test
    @SubjectAware(value = "marvin", permissions = "plugin:write")
    void shouldReturnReconnectAndLogoutLinkForFailedAuthentication() throws URISyntaxException, IOException {
      JsonNode root = requestAuthInfo(true);

      assertThat(root.get("_links").get("reconnect").get("href").asText()).isEqualTo("/v2/plugins/auth/login?reconnect=true");
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
    void shouldReturnLogoutLinkIfPermitted() throws IOException, URISyntaxException {
      JsonNode root = requestAuthInfo();

      assertThat(root.get("_links").get("logout").get("href").asText()).isEqualTo("/v2/plugins/auth");
    }

    @Test
    @SubjectAware(value = "marvin", permissions = "plugin:write")
    void shouldNotReturnLogoutLinkIfPermitted() throws IOException, URISyntaxException {
      JsonNode root = requestAuthInfo();

      assertThat(root.get("_links").get("logout").get("href").asText()).isEqualTo("/v2/plugins/auth");
    }

    private JsonNode requestAuthInfo() throws IOException, URISyntaxException {
      return requestAuthInfo(false);
    }

    private JsonNode requestAuthInfo(boolean failed) throws IOException, URISyntaxException {
      AuthenticationInfo info = new SimpleAuthenticationInfo(
        "trillian", "tricia.mcmillan@hitchhiker.com", Instant.now(), failed
      );
      when(authenticator.getAuthenticationInfo()).thenReturn(Optional.of(info));

      DisplayUser user = DisplayUser.from(UserTestData.createTrillian());
      when(userDisplayManager.get("trillian")).thenReturn(Optional.of(user));

      return getJson("/v2/plugins/auth");
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
      MockHttpResponse response = get("/v2/plugins/auth/login");
      assertError(response, ERROR_SOURCE_MISSING);
    }

    @Test
    void shouldReturnErrorRedirectWithoutPluginAuthUrlParameter() throws URISyntaxException {
      scmConfiguration.setPluginAuthUrl("");

      MockHttpResponse response = get("/v2/plugins/auth/login?source=/admin/plugins");
      assertError(response, ERROR_AUTHENTICATION_DISABLED);
    }

    @Test
    void shouldReturnErrorRedirectIfAlreadyAuthenticated() throws URISyntaxException {
      when(authenticator.isAuthenticated()).thenReturn(true);

      MockHttpResponse response = get("/v2/plugins/auth/login?source=/admin/plugins");
      assertError(response, ERROR_ALREADY_AUTHENTICATED);
    }

    @Test
    @SubjectAware("trillian")
    void shouldIgnorePreviousAuthenticationOnReconnection() throws URISyntaxException, IOException {
      lenient().when(authenticator.isAuthenticated()).thenReturn(true);
      when(challengeGenerator.create()).thenReturn("abcd");
      when(parameterSerializer.serialize(any(AuthParameter.class))).thenReturn("def");

      scmConfiguration.setPluginAuthUrl("https://plug.ins");

      MockHttpResponse response = get("/v2/plugins/auth/login?source=/admin/plugins&reconnect=true");
      assertRedirect(response, "https://plug.ins?instance=%2Fv2%2Fplugins%2Fauth%2Fcallback?params%3Ddef");
    }

    @Test
    @SubjectAware("trillian")
    void shouldReturnRedirectToPluginAuthUrl() throws URISyntaxException, IOException {
      when(challengeGenerator.create()).thenReturn("abcd");
      when(parameterSerializer.serialize(any(AuthParameter.class))).thenReturn("def");

      scmConfiguration.setPluginAuthUrl("https://plug.ins");

      MockHttpResponse response = get("/v2/plugins/auth/login?source=/admin/plugins");
      assertRedirect(response, "https://plug.ins?instance=%2Fv2%2Fplugins%2Fauth%2Fcallback?params%3Ddef");
    }

    @Test
    @SubjectAware("trillian")
    void shouldExcludeCallbackFromXsrf() throws URISyntaxException, IOException {
      when(challengeGenerator.create()).thenReturn("1234");
      when(parameterSerializer.serialize(any(AuthParameter.class))).thenReturn("def");
      scmConfiguration.setPluginAuthUrl("https://plug.ins");

      get("/v2/plugins/auth/login?source=/admin/plugins");

      verify(excludes).add("/v2/plugins/auth/callback");
    }

    @Test
    @SubjectAware("trillian")
    void shouldSendAuthParameters() throws URISyntaxException, IOException {
      when(challengeGenerator.create()).thenReturn("abc123def");
      when(parameterSerializer.serialize(any(AuthParameter.class))).thenReturn("xyz");

      get("/v2/plugins/auth/login?source=/admin/plugins");

      ArgumentCaptor<AuthParameter> captor = ArgumentCaptor.forClass(AuthParameter.class);
      verify(parameterSerializer).serialize(captor.capture());

      AuthParameter parameter = captor.getValue();
      assertThat(parameter.getChallenge()).isEqualTo("abc123def");
      assertThat(parameter.getSource()).isEqualTo("/admin/plugins");
      assertThat(parameter.getPrincipal()).isEqualTo("trillian");
    }

  }

  @Nested
  @SubjectAware("marvin")
  class AbortAuthentication {

    @BeforeEach
    void setUp() throws IOException {
      lenient().when(challengeGenerator.create()).thenReturn("xyz");
      lenient().when(parameterSerializer.serialize(any(AuthParameter.class))).thenReturn("secureParams");
    }

    @Test
    void shouldReturnErrorRedirectWithoutParams() throws URISyntaxException {
      MockHttpResponse response = get("/v2/plugins/auth/callback");
      assertError(response, ERROR_PARAMS_MISSING);
    }


    @Test
    void shouldReturnErrorRedirectWithoutChallenge() throws URISyntaxException, IOException {
      mockParams("marvin", null, "/");
      MockHttpResponse response = get("/v2/plugins/auth/callback?params=secureParams");
      assertError(response, ERROR_CHALLENGE_MISSING);
    }

    @Test
    void shouldReturnErrorRedirectWithChallengeMismatch() throws URISyntaxException, IOException {
      mockParams("marvin", "abc", "/repos");
      get("/v2/plugins/auth/login?source=/repos");
      MockHttpResponse response = get("/v2/plugins/auth/callback?params=secureParams");
      assertError(response, ERROR_CHALLENGE_DOES_NOT_MATCH);
    }

    @Test
    void shouldRedirectToRoot() throws URISyntaxException, IOException {
      mockParams("marvin", "xyz", null);
      get("/v2/plugins/auth/login?source=/repos");
      MockHttpResponse response = get("/v2/plugins/auth/callback?params=secureParams");
      assertRedirect(response, "/");
    }

    @Test
    void shouldRedirectToSource() throws URISyntaxException, IOException {
      mockParams("marvin", "xyz", "/repos");
      get("/v2/plugins/auth/login?source=/repos");
      MockHttpResponse response = get("/v2/plugins/auth/callback?params=secureParams");
      assertRedirect(response, "/repos");
    }

    @Test
    void shouldRemoveCallbackFromXsrf() throws URISyntaxException, IOException {
      mockParams("marvin", "xyz", "/repos");
      get("/v2/plugins/auth/login?source=/repos");
      get("/v2/plugins/auth/callback?params=secureParams");
      verify(excludes).remove("/v2/plugins/auth/callback");
    }

  }

  @Nested
  @SubjectAware("slarti")
  class AuthenticationCallback {

    @BeforeEach
    void setUp() throws IOException {
      lenient().when(challengeGenerator.create()).thenReturn("abc");
      lenient().when(parameterSerializer.serialize(any(AuthParameter.class))).thenReturn("secureParams");
    }

    @Test
    void shouldReturnErrorRedirectWithoutParameters() throws URISyntaxException {
      MockHttpResponse response = post("/v2/plugins/auth/callback", "trillian", "rf");
      assertError(response, ERROR_PARAMS_MISSING);
    }

    @Test
    void shouldReturnErrorRedirectWithoutChallengeParameter() throws URISyntaxException, IOException {
      mockParams("slarti", null, "/");
      MockHttpResponse response = post("/v2/plugins/auth/callback?params=secureParams", "slarti", "rf");
      assertError(response, ERROR_CHALLENGE_MISSING);
    }

    @Test
    void shouldReturnErrorRedirectWithChallengeMismatch() throws URISyntaxException, IOException {
      mockParams("slarti", "xyz", "/");
      get("/v2/plugins/auth/login?source=/repos");
      MockHttpResponse response = post("/v2/plugins/auth/callback?params=secureParams", "trillian", "rf");
      assertError(response, ERROR_CHALLENGE_DOES_NOT_MATCH);
    }

    @Test
    void shouldReturnErrorRedirectFromFailedAuthentication() throws URISyntaxException, IOException {
      mockParams("slarti", "abc", "/");
      FetchAccessTokenFailedException exception = new FetchAccessTokenFailedException("failed ...");
      doThrow(exception).when(authenticator).authenticate("slarti", "rf");
      get("/v2/plugins/auth/login?source=/repos");
      MockHttpResponse response = post("/v2/plugins/auth/callback?params=secureParams", "slarti", "rf");
      assertError(response, exception.getCode());
    }

    @Test
    void shouldAuthenticate() throws URISyntaxException, IOException {
      mockParams("slarti", "abc", "/");
      get("/v2/plugins/auth/login?source=/repos");
      post("/v2/plugins/auth/callback?params=secureParams", "slarti", "refresh_token");
      verify(authenticator).authenticate("slarti", "refresh_token");
    }

    @Test
    void shouldRedirectToSource() throws URISyntaxException, IOException {
      mockParams("slarti", "abc", "/users");
      get("/v2/plugins/auth/login?source=/users");
      MockHttpResponse response = post("/v2/plugins/auth/callback?params=secureParams", "slarti", "rrrrf");
      assertRedirect(response, "/users");
    }

    @Test
    void shouldRemoveCallbackFromXsrf() throws URISyntaxException, IOException {
      mockParams("slarti", "abc", "/users");
      get("/v2/plugins/auth/login?source=/repos");
      post("/v2/plugins/auth/callback?params=secureParams", "slarti", "rf");
      verify(excludes).remove("/v2/plugins/auth/callback");
    }

  }

  private void mockParams(String principal, String challenge, String source) throws IOException {
    AuthParameter params = new AuthParameter(principal, challenge, source);
    when(parameterSerializer.deserialize("secureParams", AuthParameter.class)).thenReturn(params);
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
    MockHttpRequest request = MockHttpRequest.get(uri);
    return request(request);
  }

  private final ObjectMapper mapper = new ObjectMapper();

  private JsonNode getJson(String uri) throws URISyntaxException, IOException {
    MockHttpResponse response = get(uri);
    return mapper.readTree(response.getContentAsString());
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
    boolean failed;
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
