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

package sonia.scm.repository.spi;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;


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
      Path hgrc = null;

      if (isModificationRequired()) {
        hgrc = Files.createTempFile(null, ".rc");
      }

      try {
        if (hgrc != null) {
          setupHgrc(hgrc.toFile());
        }

        return callable.call(hgrc);
      } finally {
        if (hgrc != null) {
          if(!hgrc.toFile().delete()) {
            LOG.warn("error cleaning up hgrc {}", hgrc.toFile().getAbsoluteFile());
          }
        }
      }
    }

    private void setupHgrc(File file) throws IOException {
      INIConfiguration hgrc = new INIConfiguration();

      if (isAuthenticationEnabled()) {
        applyAuthentication(hgrc);
      }

      if (globalProxyConfiguration.isEnabled()) {
        applyProxyConfiguration(hgrc);
      }

      INIConfigurationWriter writer = new INIConfigurationWriter();
      writer.write(hgrc, file);
    }

    private void applyProxyConfiguration(INIConfiguration hgrc) {
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
  }

  @FunctionalInterface
  public interface HgCallable<T> {
    T call(Path configFile) throws IOException;
  }

}
