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
