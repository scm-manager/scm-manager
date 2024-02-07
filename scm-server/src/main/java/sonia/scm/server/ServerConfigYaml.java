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

package sonia.scm.server;

public class ServerConfigYaml {

  private static final String SCM_SERVER_PREFIX = "SCM_";

  // ### Server
  private String addressBinding = "0.0.0.0";
  private int port = 8080;
  private String contextPath = "/scm";
  private int httpHeaderSize = 16384;
  // The default temp dir path depends on the platform
  private String tempDir = "work/scm";
  // Resolves the client ip instead of the reverse proxy ip if the X-Forwarded-For header is present
  private boolean forwardHeadersEnabled = false;
  private int idleTimeout = 0;

  // ### SSL-related config
  // Only configure SSL if the key store path is set
  private SSLConfig https = new SSLConfig();

  public static class SSLConfig {
    private String keyStorePath = "";
    private String keyStorePassword = "";
    private String keyStoreType = "PKCS12";
    // If the ssl port is set, the http port will automatically redirect to this
    private int sslPort = 8443;

    private boolean redirectHttpToHttps = false;

    public String getKeyStorePath() {
      return getEnvWithDefault("HTTPS_KEY_STORE_PATH", keyStorePath);
    }

    public String getKeyStorePassword() {
      return getEnvWithDefault("HTTPS_KEY_STORE_PASSWORD", keyStorePassword);
    }

    public String getKeyStoreType() {
      return getEnvWithDefault("HTTPS_KEY_STORE_TYPE", keyStoreType);
    }

    public int getSslPort() {
      return getEnvWithDefault("HTTPS_SSL_PORT", sslPort);
    }

    public boolean isRedirectHttpToHttps() {
      return getEnvWithDefault("HTTPS_REDIRECT_HTTP_TO_HTTPS", redirectHttpToHttps);
    }

    public void setKeyStorePath(String keyStorePath) {
      this.keyStorePath = keyStorePath;
    }

    public void setKeyStorePassword(String keyStorePassword) {
      this.keyStorePassword = keyStorePassword;
    }


    public void setKeyStoreType(String keyStoreType) {
      this.keyStoreType = keyStoreType;
    }

    public void setSslPort(int sslPort) {
      this.sslPort = sslPort;
    }

    public void setRedirectHttpToHttps(boolean redirectHttpToHttps) {
      this.redirectHttpToHttps = redirectHttpToHttps;
    }
  }

  public String getAddressBinding() {
    return getEnvWithDefault("ADDRESS_BINDING", addressBinding);
  }

  public void setAddressBinding(String addressBinding) {
    this.addressBinding = addressBinding;
  }

  public int getPort() {
    return getEnvWithDefault("PORT", port);
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getContextPath() {
    return getEnvWithDefault("CONTEXT_PATH", contextPath);
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  public int getHttpHeaderSize() {
    return getEnvWithDefault("HTTP_HEADER_SIZE", httpHeaderSize);
  }

  public void setHttpHeaderSize(int httpHeaderSize) {
    this.httpHeaderSize = httpHeaderSize;
  }

  public SSLConfig getHttps() {
    return https;
  }

  public void setHttps(SSLConfig https) {
    this.https = https;
  }

  public String getTempDir() {
    return getEnvWithDefault("TEMP_DIR", tempDir);
  }

  public void setTempDir(String tempDir) {
    this.tempDir = tempDir;
  }

  public boolean isForwardHeadersEnabled() {
    return getEnvWithDefault("FORWARD_HEADERS_ENABLED", forwardHeadersEnabled);
  }

  public void setForwardHeadersEnabled(boolean forwardHeadersEnabled) {
    this.forwardHeadersEnabled = forwardHeadersEnabled;
  }

  public int getIdleTimeout() {
    return getEnvWithDefault("IDLE_TIMEOUT", idleTimeout);
  }

  public void setIdleTimeout(int idleTimeout) {
    this.idleTimeout = idleTimeout;
  }

  static int getEnvWithDefault(String envKey, int configValue) {
    String value = getEnv(envKey);
    return value != null ? Integer.parseInt(value) : configValue;
  }

  static String getEnvWithDefault(String envKey, String configValue) {
    String value = getEnv(envKey);
    return value != null ? value : configValue;
  }

  static boolean getEnvWithDefault(String envKey, boolean configValue) {
    String value = getEnv(envKey);
    return value != null ? Boolean.parseBoolean(value) : configValue;
  }

  private static String getEnv(String envKey) {
    return System.getenv(SCM_SERVER_PREFIX + envKey);
  }
}
