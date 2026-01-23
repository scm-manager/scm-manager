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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INISection;
import sonia.scm.net.GlobalProxyConfiguration;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TemporaryConfigFactoryTest {

  @Mock
  private HgCommandContext commandContext;
  private TemporaryConfigFactory configFactory;
  private ScmConfiguration configuration;

  @BeforeEach
  void setUp(@TempDir Path directory) throws IOException {
    configuration = new ScmConfiguration();
    configFactory = new TemporaryConfigFactory(new GlobalProxyConfiguration(configuration));
  }

  @Test
  void shouldNotCreateHgrc() throws IOException {
    configFactory.withContext(commandContext).call((Path configFile) -> {
      assertThat(configFile).isNull();
      return null;
    });
  }


  @Test
  void shouldCreateHgrcWithAuthentication() throws IOException {
    configFactory
      .withContext(commandContext)
      .withCredentials("https://hg.hitchhiker.org/repo", "trillian", "secret")
      .call((Path configFile) -> {
        INIConfiguration ini = assertHgrc(configFile);

        INISection auth = ini.getSection("auth");
        assertThat(auth).isNotNull();
        assertThat(auth.getParameter("temporary.prefix")).isEqualTo("hg.hitchhiker.org");
        assertThat(auth.getParameter("temporary.schemes")).isEqualTo("https");
        assertThat(auth.getParameter("temporary.username")).isEqualTo("trillian");
        assertThat(auth.getParameter("temporary.password")).isEqualTo("secret");

        return null;
      });
  }

  private INIConfiguration assertHgrc(Path hgrc) throws IOException {
    assertThat(hgrc).exists();

    INIConfigurationReader reader = new INIConfigurationReader();
    return reader.read(hgrc.toFile());
  }

  @Test
  void shouldCreateHgrcWithSimpleProxyConfiguration() throws IOException {
    configuration.setEnableProxy(true);
    configuration.setProxyServer("proxy.hitchhiker.org");
    configuration.setProxyPort(3128);
    configFactory.withContext(commandContext).call((Path configFile) -> {
      INIConfiguration ini = assertHgrc(configFile);

      INISection proxy = ini.getSection("http_proxy");
      assertThat(proxy).isNotNull();
      assertThat(proxy.getParameter("host")).isEqualTo("proxy.hitchhiker.org:3128");
      return null;
    });
  }

  @Test
  void shouldCreateHgrcProxyConfigurationWithAuthentication() throws IOException {
    configuration.setEnableProxy(true);
    configuration.setProxyServer("proxy.hitchhiker.org");
    configuration.setProxyPort(3128);
    configuration.setProxyUser("trillian");
    configuration.setProxyPassword("secret");

    configFactory.withContext(commandContext).call((Path configFile) -> {
      INIConfiguration ini = assertHgrc(configFile);

      INISection proxy = ini.getSection("http_proxy");
      assertThat(proxy).isNotNull();
      assertThat(proxy.getParameter("host")).isEqualTo("proxy.hitchhiker.org:3128");
      assertThat(proxy.getParameter("user")).isEqualTo("trillian");
      assertThat(proxy.getParameter("passwd")).isEqualTo("secret");
      return null;
    });
  }

  @Test
  void shouldCreateHgrcProxyConfigurationWithNoExcludes() throws IOException {
    configuration.setEnableProxy(true);
    configuration.setProxyServer("proxy.hitchhiker.org");
    configuration.setProxyPort(3128);
    configuration.setProxyExcludes(ImmutableSet.of("hg.hitchhiker.org", "localhost", "127.0.0.1"));

    configFactory.withContext(commandContext).call((Path configFile) -> {
      INIConfiguration ini = assertHgrc(configFile);

      INISection proxy = ini.getSection("http_proxy");
      assertThat(proxy).isNotNull();
      assertThat(proxy.getParameter("no")).isEqualTo("hg.hitchhiker.org,localhost,127.0.0.1");
      return null;
    });
  }

  @Test
  void shouldRemoveCreatedHgrc() throws IOException {
    Path hgrc = configFactory
      .withContext(commandContext)
      .withCredentials("https://hg.hitchhiker.com", "marvin", "brainLikeAPlanet")
      .call((Path configFile) -> {
        assertThat(configFile).exists();
        return configFile;
      });
    assertThat(hgrc).doesNotExist();
  }
}
