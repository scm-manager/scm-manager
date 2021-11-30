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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Data;
import lombok.Value;
import org.apache.shiro.SecurityUtils;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Optional;

import static sonia.scm.plugin.Tracing.SPAN_KIND;

@Singleton
public class PluginCenterAuthenticator {

  @VisibleForTesting
  static final String STORE_NAME = "plugin-center-auth";

  private final ConfigurationStore<Authentication> configurationStore;
  private final ScmConfiguration scmConfiguration;
  private final AdvancedHttpClient advancedHttpClient;

  @Inject
  public PluginCenterAuthenticator(ConfigurationStoreFactory configurationStore, ScmConfiguration scmConfiguration, AdvancedHttpClient advancedHttpClient) {
    this.configurationStore = configurationStore.withType(Authentication.class).withName(STORE_NAME).build();
    this.scmConfiguration = scmConfiguration;
    this.advancedHttpClient = advancedHttpClient;
  }

  public void authenticate(String refreshToken) {
    PluginPermissions.write().check();

    Preconditions.checkArgument(!Strings.isNullOrEmpty(refreshToken), "refresh token is required");
    // check if refresh token is valid
    fetchAccessToken(principal(), refreshToken);
  }

  public boolean isAuthenticated() {
    return getAuthentication().isPresent();
  }

  public String fetchAccessToken() {
    PluginPermissions.write().check();
    Authentication authentication = getAuthentication()
      .orElseThrow(() -> new IllegalStateException("An access token can only be obtained, after a prior authentication"));
    return fetchAccessToken(authentication.getPrincipal(), authentication.getRefreshToken());
  }

  @CanIgnoreReturnValue
  private String fetchAccessToken(String principal, String refreshToken) {
    String pluginAuthUrl = scmConfiguration.getPluginAuthUrl();
    Preconditions.checkState(!Strings.isNullOrEmpty(pluginAuthUrl), "plugin auth url is not configured");

    try {
      AdvancedHttpResponse response = advancedHttpClient.post(HttpUtil.concatenate(pluginAuthUrl, "refresh"))
        .spanKind(SPAN_KIND)
        .jsonContent(new RefreshRequest(refreshToken))
        .request();

      if (!response.isSuccessful()) {
        throw new FetchAccessTokenFailedException("failed to obtain access token, server returned status code " + response.getStatus());
      }

      RefreshResponse refresh = response.contentFromJson(RefreshResponse.class);

      Authentication authentication = new Authentication();
      authentication.setPrincipal(principal);
      authentication.setRefreshToken(refresh.getRefreshToken());
      configurationStore.set(authentication);

      return refresh.getAccessToken();
    } catch (IOException ex) {
      throw new FetchAccessTokenFailedException("failed to obtain an access token", ex);
    }
  }

  private String principal() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  private Optional<Authentication> getAuthentication() {
    return configurationStore.getOptional();
  }

  @Data
  @XmlRootElement
  @VisibleForTesting
  @XmlAccessorType(XmlAccessType.FIELD)
  static class Authentication {
    private String principal;
    private String refreshToken;
  }

  @Value
  public static class RefreshRequest {
    @JsonProperty("refresh_token")
    String refreshToken;
  }

  @Data
  public static class RefreshResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
  }
}
