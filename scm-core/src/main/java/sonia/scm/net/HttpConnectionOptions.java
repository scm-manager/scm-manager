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

import com.google.common.annotations.VisibleForTesting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Options for establishing a http connection.
 * The options can be used to create a new http connection
 * with {@link HttpURLConnectionFactory#create(URL, HttpConnectionOptions)}.
 *
 * @since 2.23.0
 */
@Getter
@ToString
@EqualsAndHashCode
public final class HttpConnectionOptions {

  @VisibleForTesting
  static final int DEFAULT_CONNECTION_TIMEOUT = 30000;

  @VisibleForTesting
  static final int DEFAULT_READ_TIMEOUT = 1200000;

  @Nullable
  private ProxyConfiguration proxyConfiguration;

  @Nullable
  private KeyManager[] keyManagers;

  private boolean disableCertificateValidation = false;
  private boolean disableHostnameValidation = false;
  private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
  private int readTimeout = DEFAULT_READ_TIMEOUT;
  private boolean ignoreProxySettings = false;
  private Map<String, String> connectionProperties = new HashMap<>();

  /**
   * Returns optional local proxy configuration.
   * @return local proxy configuration or empty optional
   */
  public Optional<ProxyConfiguration> getProxyConfiguration() {
    return Optional.ofNullable(proxyConfiguration);
  }

  /**
   * Return optional array of key managers for client certificate authentication.
   *
   * @return array of key managers or empty optional
   */
  public Optional<KeyManager[]> getKeyManagers() {
    if (keyManagers != null) {
      return Optional.of(Arrays.copyOf(keyManagers, keyManagers.length));
    }
    return Optional.empty();
  }

  public Map<String, String> getConnectionProperties() {
    return Collections.unmodifiableMap(connectionProperties);
  }

  /**
   * Disable certificate validation.
   * <b>WARNING:</b> This option should only be used for internal test.
   * It should never be used in production, because it is high security risk.
   *
   * @return {@code this}
   */
  public HttpConnectionOptions withDisableCertificateValidation() {
    this.disableCertificateValidation = true;
    return this;
  }

  /**
   * Disable hostname validation.
   * <b>WARNING:</b> This option should only be used for internal test.
   * It should never be used in production, because it is high security risk.
   *
   * @return {@code this}
   */
  public HttpConnectionOptions withDisabledHostnameValidation() {
    this.disableHostnameValidation = true;
    return this;
  }

  /**
   * Configure the connection timeout.
   * @param timeout timeout
   * @param unit unit of the timeout
   * @return {@code this}
   */
  public HttpConnectionOptions withConnectionTimeout(long timeout, TimeUnit unit) {
    this.connectionTimeout = (int) unit.toMillis(timeout);
    return this;
  }

  /**
   * Configure the read timeout.
   * @param timeout timeout
   * @param unit unit of the timeout
   * @return {@code this}
   */
  public HttpConnectionOptions withReadTimeout(long timeout, TimeUnit unit) {
    this.readTimeout = (int) unit.toMillis(timeout);
    return this;
  }

  /**
   * Configure a local proxy configuration, if no configuration is set the global default configuration will be used.
   * @param proxyConfiguration local proxy configuration
   * @return {@code this}
   */
  public HttpConnectionOptions withProxyConfiguration(ProxyConfiguration proxyConfiguration) {
    this.proxyConfiguration = proxyConfiguration;
    return this;
  }

  /**
   * Configure key managers for client certificate authentication.
   * @param keyManagers key managers
   * @return {@code this}
   */
  public HttpConnectionOptions withKeyManagers(@Nullable KeyManager... keyManagers) {
    if (keyManagers != null) {
      this.keyManagers = Arrays.copyOf(keyManagers, keyManagers.length);
    }
    return this;
  }

  /**
   * Ignore proxy settings completely regardless if a local proxy configuration or a global configuration is configured.
   * @return {@code this}
   */
  public HttpConnectionOptions withIgnoreProxySettings() {
    this.ignoreProxySettings = true;
    return this;
  }

  /**
   * Add a request property that will be converted to headers in the request.
   * @param key The property (aka header) name (e.g. "User-Agent").
   * @param value The value of the property.
   * @return {@code this}
   */
  public HttpConnectionOptions addRequestProperty(String key, String value) {
    connectionProperties.put(key, value);
    return this;
  }
}
