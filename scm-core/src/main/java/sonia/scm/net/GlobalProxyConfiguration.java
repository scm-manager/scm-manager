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

package sonia.scm.net;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import sonia.scm.config.ScmConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * The {@link GlobalProxyConfiguration} is an adapter between {@link ProxyConfiguration} and {@link ScmConfiguration}.
 * Whenever proxy settings are required, the {@link GlobalProxyConfiguration} should be used instead of using
 * {@link ScmConfiguration} directly. This makes it easier to support local proxy configurations.
 *
 * @since 2.23.0
 */
public final class GlobalProxyConfiguration implements ProxyConfiguration {

  private final ScmConfiguration configuration;

  @Inject
  public GlobalProxyConfiguration(ScmConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public boolean isEnabled() {
    return configuration.isEnableProxy();
  }

  @Override
  public String getHost() {
    return configuration.getProxyServer();
  }

  @Override
  public int getPort() {
    return configuration.getProxyPort();
  }

  @Override
  public Collection<String> getExcludes() {
    Set<String> excludes = configuration.getProxyExcludes();
    if (excludes == null) {
      return Collections.emptyList();
    }
    return excludes;
  }

  @Override
  public String getUsername() {
    return configuration.getProxyUser();
  }

  @Override
  public String getPassword() {
    return configuration.getProxyPassword();
  }

  @Override
  public boolean isAuthenticationRequired() {
    return !Strings.isNullOrEmpty(getUsername()) && !Strings.isNullOrEmpty(getPassword());
  }
}
