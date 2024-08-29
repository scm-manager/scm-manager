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

package sonia.scm.repository.spi;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class TemporaryConfigFactory {

  private static final Logger LOG = LoggerFactory.getLogger(TemporaryConfigFactory.class);

  private static final String SECTION_PROXY = "http_proxy";
  private static final String SECTION_AUTH = "auth";
  private static final String AUTH_PREFIX = "temporary.";

  private final GlobalProxyConfiguration globalProxyConfiguration;

  @Inject
  public TemporaryConfigFactory(GlobalProxyConfiguration globalProxyConfiguration) {
    this.globalProxyConfiguration = globalProxyConfiguration;
  }

  public Builder withContext(HgCommandContext context) {
    return new Builder(context);
  }

  public class Builder {

    private final HgCommandContext context;
    private String url;
    private String username;
    private String password;

    private INIConfiguration hgrc;
    private INISection previousProxyConfiguration;

    private Builder(HgCommandContext context) {
      this.context = context;
    }

    public Builder withCredentials(String url, String username, String password) {
      this.url = url;
      this.username = username;
      this.password = password;
      return this;
    }

    @SuppressWarnings("java:S4042") // we know that we delete a file
    public <T> T call(HgCallable<T> callable) throws IOException {
      File file = new File(context.getDirectory(), HgRepositoryHandler.PATH_HGRC);
      boolean exists = file.exists();
      if (isModificationRequired()) {
        setupHgrc(file);
      }
      try {
        return callable.call();
      } finally {
        if (!exists && file.exists() && !file.delete()) {
          LOG.error("failed to delete temporary hgrc {}", file);
        } else if (exists && file.exists()) {
          try {
            if (hgrc != null) {
              cleanUpHgrc(file);
            }
          } catch (Exception e) {
            LOG.warn("error cleaning up hgrc", e);
          }
        }
      }
    }

    private void write(File file) throws IOException {
      INIConfigurationWriter writer = new INIConfigurationWriter();
      writer.write(hgrc, file);
    }

    private void setupHgrc(File file) throws IOException {
      if (file.exists()) {
        INIConfigurationReader reader = new INIConfigurationReader();
        hgrc = reader.read(file);
      } else {
        hgrc = new INIConfiguration();
      }

      if (isAuthenticationEnabled()) {
        applyAuthentication(hgrc);
      }

      if (globalProxyConfiguration.isEnabled()) {
        applyProxyConfiguration(hgrc);
      }

      write(file);
    }

    private void applyProxyConfiguration(INIConfiguration hgrc) {
      previousProxyConfiguration = hgrc.getSection(SECTION_PROXY);
      hgrc.removeSection(SECTION_PROXY);
      INISection proxy = new INISection(SECTION_PROXY);
      proxy.setParameter("host", globalProxyConfiguration.getHost() + ":" + globalProxyConfiguration.getPort());

      String user = globalProxyConfiguration.getUsername();
      String passwd = globalProxyConfiguration.getPassword();
      if (!Strings.isNullOrEmpty(user) && !Strings.isNullOrEmpty(passwd)) {
        proxy.setParameter("user", user);
        proxy.setParameter("passwd", passwd);
      }

      if (Util.isNotEmpty(globalProxyConfiguration.getExcludes())) {
        proxy.setParameter("no", Joiner.on(',').join(globalProxyConfiguration.getExcludes()));
      }

      hgrc.addSection(proxy);
    }

    private void applyAuthentication(INIConfiguration hgrc) {
      INISection auth = hgrc.getSection(SECTION_AUTH);
      if (auth == null) {
        auth = new INISection(SECTION_AUTH);
        hgrc.addSection(auth);
      }

      URI uri = URI.create(url);
      auth.setParameter(AUTH_PREFIX + "prefix", uri.getHost());
      auth.setParameter(AUTH_PREFIX + "schemes", uri.getScheme());
      auth.setParameter(AUTH_PREFIX + "username", username);
      auth.setParameter(AUTH_PREFIX + "password", password);

    }

    private boolean isModificationRequired() {
      return isAuthenticationEnabled() || globalProxyConfiguration.isEnabled();
    }

    private boolean isAuthenticationEnabled() {
      return !Strings.isNullOrEmpty(url)
        && !Strings.isNullOrEmpty(username)
        && !Strings.isNullOrEmpty(password);
    }

    private void cleanUpHgrc(File file) throws IOException {
      INISection auth = hgrc.getSection(SECTION_AUTH);
      if (isAuthenticationEnabled() && auth != null) {
        for (String key : auth.getParameterKeys()) {
          if (key.startsWith(AUTH_PREFIX)) {
            auth.removeParameter(key);
          }
        }
      }

      if (globalProxyConfiguration.isEnabled()) {
        hgrc.removeSection(SECTION_PROXY);
        if (previousProxyConfiguration != null) {
          hgrc.addSection(previousProxyConfiguration);
        }
      }

      if (isModificationRequired()) {
        write(file);
      }
    }

  }

  @FunctionalInterface
  public interface HgCallable<T> {
    T call() throws IOException;
  }

}
