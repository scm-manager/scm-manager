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

package sonia.scm.plugin;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.shiro.authz.AuthorizationException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.plugin.PluginCenterAuthenticator.RefreshResponse;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static sonia.scm.plugin.PluginCenterAuthenticator.*;
import static sonia.scm.plugin.PluginCenterAuthenticator.RefreshRequest;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class PluginCenterAuthenticatorTest {

  private PluginCenterAuthenticator authenticator;

  @Mock
  private AdvancedHttpClient advancedHttpClient;

  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequestWithBody request;

  private ScmConfiguration scmConfiguration;

  @Mock
  private ScmEventBus eventBus;

  private final InMemoryConfigurationStoreFactory factory = InMemoryConfigurationStoreFactory.create();

  @BeforeEach
  void setUpObjectUnderTest() {
    scmConfiguration = new ScmConfiguration();
    authenticator = new PluginCenterAuthenticator(factory, scmConfiguration, advancedHttpClient, eventBus);
  }

  @Test
  @SubjectAware("marvin")
  void shouldFailAuthenticationWithoutPermissions() {
    assertThrows(AuthorizationException.class, () -> authenticator.authenticate("marvin@hitchhiker.com", "refresh-token"));
  }

  @Test
  @SubjectAware(value = "marvin", permissions = "plugin:read")
  void shouldFailAuthenticationWithReadPermissions() {
    assertThrows(AuthorizationException.class, () -> authenticator.authenticate("marvin@hitchhiker.com", "refresh-token"));
  }

  @Test
  @SubjectAware("marvin")
  void shouldFailToFetchAccessTokenWithoutPermission() {
    assertThrows(AuthorizationException.class, () -> authenticator.fetchAccessToken());
  }

  @Test
  @SubjectAware("marvin")
  void shouldFailGetAuthenticationInfoWithoutPermission() {
    assertThrows(AuthorizationException.class, () -> authenticator.getAuthenticationInfo());
  }

  @Test
  @SubjectAware("marvin")
  void shouldFailLogoutWithoutPermission() {
    assertThrows(AuthorizationException.class, () -> authenticator.logout());
  }

  @Nested
  @SubjectAware(value = "trillian", permissions = {"plugin:read", "plugin:write"})
  class WithPermissions {

    @Test
    void shouldReturnFalseWithoutRefreshToken() {
      assertThat(authenticator.isAuthenticated()).isFalse();
    }

    @Test
    void shouldFailWithoutRefreshToken() {
      assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate("tricia.mcmillan@hitchhiker.com", null));
    }

    @Test
    void shouldFailWithEmptyRefreshToken() {
      assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate("tricia.mcmillan@hitchhiker.com", ""));
    }

    @Test
    void shouldFailWithoutSubject() {
      assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate(null, "rf"));
    }

    @Test
    void shouldFailWithEmptySubject() {
      assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate("", "rf"));
    }

    @Test
    void shouldFailWithoutPluginAuthUrl() {
      scmConfiguration.setPluginAuthUrl(null);
      assertThrows(IllegalStateException.class, () -> authenticator.authenticate("tricia.mcmillan@hitchhiker.com", "my-awesome-refresh-token"));
    }

    @Test
    void shouldAuthenticate() throws IOException {
      mockAuthProtocol("https://plugin-center-api.scm-manager.org/api/v1/auth/oidc/refresh", "access", "refresh");

      authenticator.authenticate("tricia.mcmillan@hitchhiker.com", "my-awesome-refresh-token");
      assertThat(authenticator.isAuthenticated()).isTrue();
    }

    @Test
    void shouldFireLoginEvent() throws IOException {
      mockAuthProtocol("https://plugin-center-api.scm-manager.org/api/v1/auth/oidc/refresh", "access", "refresh");

      authenticator.authenticate("tricia.mcmillan@hitchhiker.com", "my-awesome-refresh-token");

      ArgumentCaptor<PluginCenterLoginEvent> captor = ArgumentCaptor.forClass(PluginCenterLoginEvent.class);
      verify(eventBus).post(captor.capture());

      AuthenticationInfo info = captor.getValue().getAuthenticationInfo();
      assertThat(info.getPluginCenterSubject()).isEqualTo("tricia.mcmillan@hitchhiker.com");
    }

    @Test
    void shouldFailFetchWithoutPriorAuthentication() {
      assertThrows(IllegalStateException.class, () -> authenticator.fetchAccessToken());
    }

    @Test
    void shouldUseUrlFromScmConfiguration() throws IOException {
      preAuth("cool-refresh-token");
      scmConfiguration.setPluginAuthUrl("https://pca.org/oidc/");
      mockAuthProtocol("https://pca.org/oidc/refresh", "access", "refresh");

      String accessToken = authenticator.fetchAccessToken();
      assertThat(accessToken).isEqualTo("access");
    }

    @Test
    void shouldFailIfFetchFails() throws IOException {
      preAuth("cool-refresh-token");
      scmConfiguration.setPluginAuthUrl("https://plug.ins/oidc/");

      when(advancedHttpClient.post("https://plug.ins/oidc/refresh")).thenReturn(request);
      when(request.request()).thenThrow(new IOException("network down down down"));

      assertThrows(FetchAccessTokenFailedException.class, () -> authenticator.fetchAccessToken());
    }

    @Test
    void shouldFailIfFetchResponseIsNotSuccessful() throws IOException {
      preAuth("cool-refresh-token");
      scmConfiguration.setPluginAuthUrl("https://plug.ins/oidc/");

      when(advancedHttpClient.post("https://plug.ins/oidc/refresh")).thenReturn(request);

      AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
      when(request.request()).thenReturn(response);

      when(response.isSuccessful()).thenReturn(false);

      assertThrows(FetchAccessTokenFailedException.class, () -> authenticator.fetchAccessToken());
    }

    @Test
    void shouldFetchAccessToken() throws IOException {
      preAuth("cool-refresh-token");
      mockAuthProtocol("https://plugin-center-api.scm-manager.org/api/v1/auth/oidc/refresh", "access", "refresh");

      String accessToken = authenticator.fetchAccessToken();
      assertThat(accessToken).isEqualTo("access");
    }

    @Test
    void shouldStoreRefreshTokenAfterFetch() throws IOException {
      preAuth("refreshOne");
      mockAuthProtocol("https://plugin-center-api.scm-manager.org/api/v1/auth/oidc/refresh", "accessTwo", "refreshTwo");

      authenticator.fetchAccessToken();
      authenticator.fetchAccessToken();

      ArgumentCaptor<RefreshRequest> captor = ArgumentCaptor.forClass(RefreshRequest.class);
      verify(request, times(2)).jsonContent(captor.capture());

      List<String> refreshTokens = captor.getAllValues()
        .stream()
        .map(RefreshRequest::getRefreshToken)
        .collect(Collectors.toList());

      assertThat(refreshTokens).containsExactlyInAnyOrder("refreshOne", "refreshTwo");
    }

    @Test
    void shouldReturnEmptyWithoutPriorAuthentication() {
      assertThat(authenticator.getAuthenticationInfo()).isEmpty();
    }

    @Test
    void shouldReturnAuthenticationInfo() {
      preAuth("refresh_token");
      assertThat(authenticator.getAuthenticationInfo()).hasValueSatisfying(info -> {
        assertThat(info.getPluginCenterSubject()).isEqualTo("tricia.mcmillan@hitchhiker.com");
        assertThat(info.getPrincipal()).isEqualTo("trillian");
        assertThat(info.getDate()).isNotNull();
      });
    }

    @Test
    void shouldLogout() {
      preAuth("refresh_token");

      authenticator.logout();

      assertThat(authenticator.isAuthenticated()).isFalse();
      assertThat(authenticator.getAuthenticationInfo()).isEmpty();
    }

    @Test
    void shouldFireLogoutEventAfterLogout() {
      preAuth("refresh_token");

      authenticator.logout();

      ArgumentCaptor<PluginCenterLogoutEvent> captor = ArgumentCaptor.forClass(PluginCenterLogoutEvent.class);
      verify(eventBus).post(captor.capture());

      AuthenticationInfo info = captor.getValue().getPriorAuthenticationInfo();
      assertThat(info.getPluginCenterSubject()).isEqualTo("tricia.mcmillan@hitchhiker.com");
    }

    @SuppressWarnings("unchecked")
    private void preAuth(String refreshToken) {
      Authentication authentication = new Authentication();
      authentication.setPluginCenterSubject("tricia.mcmillan@hitchhiker.com");
      authentication.setPrincipal("trillian");
      authentication.setRefreshToken(refreshToken);
      authentication.setDate(Instant.now());
      factory.get(STORE_NAME, null).set(authentication);
    }

    @CanIgnoreReturnValue
    private void mockAuthProtocol(String url, String accessToken, String refreshToken) throws IOException {
      when(advancedHttpClient.post(url)).thenReturn(request);

      AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
      when(request.request()).thenReturn(response);

      RefreshResponse refreshResponse = new RefreshResponse();
      refreshResponse.setAccessToken(accessToken);
      refreshResponse.setRefreshToken(refreshToken);
      when(response.contentFromJson(RefreshResponse.class)).thenReturn(refreshResponse);

      when(response.isSuccessful()).thenReturn(true);
    }

  }

}
