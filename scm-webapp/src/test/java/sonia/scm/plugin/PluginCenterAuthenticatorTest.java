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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.plugin.PluginCenterAuthenticator.RefreshResponse;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static sonia.scm.plugin.PluginCenterAuthenticator.*;
import static sonia.scm.plugin.PluginCenterAuthenticator.RefreshRequest;

@ExtendWith(MockitoExtension.class)
class PluginCenterAuthenticatorTest {

  private PluginCenterAuthenticator authenticator;

  @Mock
  private AdvancedHttpClient advancedHttpClient;

  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequestWithBody request;

  private ScmConfiguration scmConfiguration;

  private final InMemoryConfigurationStoreFactory factory = InMemoryConfigurationStoreFactory.create();

  @BeforeEach
  void setUpObjectUnderTest() {
    scmConfiguration =  new ScmConfiguration();
    authenticator = new PluginCenterAuthenticator(factory, scmConfiguration, advancedHttpClient);
  }

  @Test
  void shouldReturnFalseWithoutRefreshToken() {
    assertThat(authenticator.isAuthenticated()).isFalse();
  }

  @Test
  void shouldFailWithoutRefreshToken() {
    assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate(null));
  }

  @Test
  void shouldFailWithEmptyRefreshToken() {
    assertThrows(IllegalArgumentException.class, () -> authenticator.authenticate(""));
  }

  @Test
  void shouldFailWithoutPluginAuthUrl() {
    scmConfiguration.setPluginAuthUrl(null);
    // TODO
    assertThrows(IllegalStateException.class, () -> authenticator.authenticate("my-awesome-refresh-token"));
  }

  @Test
  void shouldAuthenticate() throws IOException {
    mockAuthProtocol("https://plugin-center-api.scm-manager.org/api/v1/auth/oidc/refresh", "access", "refresh");

    authenticator.authenticate("my-awesome-refresh-token");
    assertThat(authenticator.isAuthenticated()).isTrue();
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

  @SuppressWarnings("unchecked")
  private void preAuth(String refreshToken) {
    Authentication authentication = new Authentication();
    authentication.setRefreshToken(refreshToken);
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
