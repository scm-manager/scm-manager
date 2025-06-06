/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.xml.XmlEncryptionAdapter;
import sonia.scm.xml.XmlInstantAdapter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static sonia.scm.plugin.Tracing.SPAN_KIND;

@Singleton
public class PluginCenterAuthenticator {

  private static final Logger LOG = LoggerFactory.getLogger(PluginCenterAuthenticator.class);

  @VisibleForTesting
  static final String STORE_NAME = "plugin-center-auth";

  private final ConfigurationStore<Authentication> configurationStore;
  private final ScmConfiguration scmConfiguration;
  private final AdvancedHttpClient advancedHttpClient;
  private final ScmEventBus eventBus;

  @Inject
  public PluginCenterAuthenticator(
    ConfigurationStoreFactory configurationStore, ScmConfiguration scmConfiguration,
    AdvancedHttpClient advancedHttpClient, ScmEventBus eventBus
  ) {
    this.configurationStore = configurationStore.withType(Authentication.class).withName(STORE_NAME).build();
    this.scmConfiguration = scmConfiguration;
    this.advancedHttpClient = advancedHttpClient;
    this.eventBus = eventBus;
  }

  public void authenticate(String pluginCenterSubject, String refreshToken) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(pluginCenterSubject), "pluginCenterSubject is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(refreshToken), "refresh token is required");

    // only a user which is able to manage plugins, can authenticate the plugin center
    PluginPermissions.write().check();

    // check if refresh token is valid
    Authentication authentication = new Authentication(
      principal(), pluginCenterSubject, refreshToken, Instant.now(), false
    );
    fetchAccessToken(authentication);
    eventBus.post(new PluginCenterLoginEvent(authentication));
  }

  public void logout() {
    PluginPermissions.write().check();

    getAuthenticationInfo().ifPresent(authenticationInfo -> {
      eventBus.post(new PluginCenterLogoutEvent(authenticationInfo));
      configurationStore.delete();
    });
  }

  public boolean isAuthenticated() {
    return getAuthentication().isPresent();
  }

  public Optional<AuthenticationInfo> getAuthenticationInfo() {
    PluginPermissions.read().check();
    return getAuthentication().map(a -> a);
  }

  public Optional<String> fetchAccessToken() {
    PluginPermissions.read().check();
    Authentication authentication = getAuthentication()
      .orElseThrow(() -> new IllegalStateException("An access token can only be obtained, after a prior authentication"));
    try {
      return Optional.of(fetchAccessToken(authentication));
    } catch (FetchAccessTokenFailedException ex) {
      LOG.warn("failed to fetch access token", ex);
      return Optional.empty();
    }
  }

  @CanIgnoreReturnValue
  private String fetchAccessToken(Authentication authentication) {
    String pluginAuthUrl = scmConfiguration.getPluginAuthUrl();
    Preconditions.checkState(!Strings.isNullOrEmpty(pluginAuthUrl), "plugin auth url is not configured");

    try {
      AdvancedHttpResponse response = advancedHttpClient.post(HttpUtil.concatenate(pluginAuthUrl, "refresh"))
        .spanKind(SPAN_KIND)
        .jsonContent(new RefreshRequest(authentication.getRefreshToken()))
        .request();

      if (!response.isSuccessful()) {
        authenticationFailed(authentication);
        throw new FetchAccessTokenFailedException("failed to obtain access token, server returned status code " + response.getStatus());
      }

      RefreshResponse refresh = response.contentFromJson(RefreshResponse.class);

      authentication.setRefreshToken(refresh.getRefreshToken());
      authentication.setFailed(false);
      configurationStore.set(authentication);

      return refresh.getAccessToken();
    } catch (IOException ex) {
      authenticationFailed(authentication);
      throw new FetchAccessTokenFailedException("failed to obtain an access token", ex);
    }
  }

  private void authenticationFailed(Authentication authentication) {
    authentication.setFailed(true);
    configurationStore.set(authentication);
    eventBus.post(new PluginCenterAuthenticationFailedEvent(authentication));
  }

  private String principal() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  private Optional<Authentication> getAuthentication() {
    return configurationStore.getOptional();
  }

  @Data
  @XmlRootElement
  @AllArgsConstructor
  @NoArgsConstructor
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Authentication implements AuthenticationInfo {
    private String principal;
    private String pluginCenterSubject;
    @XmlJavaTypeAdapter(XmlEncryptionAdapter.class)
    private String refreshToken;
    @XmlJavaTypeAdapter(XmlInstantAdapter.class)
    private Instant date;
    private boolean failed;
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
