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
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.net.GlobalProxyConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TemporaryConfigFactoryTest {

  @Mock
  private HgCommandContext commandContext;
  private TemporaryConfigFactory configFactory;
  private ScmConfiguration configuration;

  private Path hgrc;

  @BeforeEach
  void setUp(@TempDir Path directory) throws IOException {
    Path hg = Files.createDirectories(directory.resolve(".hg"));
    hgrc = hg.resolve("hgrc");
    lenient().when(commandContext.getDirectory()).thenReturn(directory.toFile());

    configuration = new ScmConfiguration();
    configFactory = new TemporaryConfigFactory(new GlobalProxyConfiguration(configuration));
  }

  @Test
  void shouldNotCreateHgrc() throws IOException {
    configFactory.withContext(commandContext).call(() -> {
      assertThat(hgrc).doesNotExist();
      return null;
    });
  }

  @Test
  @SuppressWarnings("java:S2699") // test should just ensure that no exception is thrown
  void shouldNotFailIfFileExistsButWithoutProxyOrAuthentication() throws IOException {
    Files.createFile(hgrc);
    configFactory.withContext(commandContext).call(() -> null);
  }

  @Test
  void shouldCreateHgrcWithAuthentication() throws IOException {
    configFactory
      .withContext(commandContext)
      .withCredentials("https://hg.hitchhiker.org/repo", "trillian", "secret")
      .call(() -> {
        INIConfiguration ini = assertHgrc();

        INISection auth = ini.getSection("auth");
        assertThat(auth).isNotNull();
        assertThat(auth.getParameter("temporary.prefix")).isEqualTo("hg.hitchhiker.org");
        assertThat(auth.getParameter("temporary.schemes")).isEqualTo("https");
        assertThat(auth.getParameter("temporary.username")).isEqualTo("trillian");
        assertThat(auth.getParameter("temporary.password")).isEqualTo("secret");

        return null;
      });
  }

  private INIConfiguration assertHgrc() throws IOException {
    assertThat(hgrc).exists();

    INIConfigurationReader reader = new INIConfigurationReader();
    return reader.read(hgrc.toFile());
  }

  @Test
  void shouldCreateHgrcWithSimpleProxyConfiguration() throws IOException {
    configuration.setEnableProxy(true);
    configuration.setProxyServer("proxy.hitchhiker.org");
    configuration.setProxyPort(3128);
    configFactory.withContext(commandContext).call(() -> {
      INIConfiguration ini = assertHgrc();

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

    configFactory.withContext(commandContext).call(() -> {
      INIConfiguration ini = assertHgrc();

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

    configFactory.withContext(commandContext).call(() -> {
      INIConfiguration ini = assertHgrc();

      INISection proxy = ini.getSection("http_proxy");
      assertThat(proxy).isNotNull();
      assertThat(proxy.getParameter("no")).isEqualTo("hg.hitchhiker.org,localhost,127.0.0.1");
      return null;
    });
  }

  @Test
  void shouldRemoveCreatedHgrc() throws IOException {
    configFactory
      .withContext(commandContext)
      .withCredentials("https://hg.hitchhiker.com", "marvin", "brainLikeAPlanet")
      .call(() -> {
        assertThat(hgrc).exists();
        return null;
      });
    assertThat(hgrc).doesNotExist();
  }

  @Test
  void shouldKeepAuthenticationInformation() throws IOException {
    writeAuthentication();

    configFactory
      .withContext(commandContext)
      .withCredentials("https://hg.hitchhiker.com", "marvin", "brainLikeAPlanet")
      .call(() -> {
        INIConfiguration configuration = assertHgrc();
        INISection auth = configuration.getSection("auth");
        assertThat(auth).isNotNull();
        assertThat(auth.getParameter("a.username")).isEqualTo("dent");
        assertThat(auth.getParameter("temporary.username")).isEqualTo("marvin");
        return null;
      });

    INIConfiguration configuration = assertHgrc();
    INISection auth = configuration.getSection("auth");
    assertThat(auth).isNotNull();
    assertThat(auth.getParameter("a.username")).isEqualTo("dent");
    assertThat(auth.getParameter("temporary.username")).isNull();
  }

  @Test
  void shouldRestoreProxyConfiguration() throws IOException {
    writeProxyConfiguration();

    configuration.setEnableProxy(true);
    configuration.setProxyServer("proxy.hitchhiker.org");
    configuration.setProxyPort(3128);

    configFactory.withContext(commandContext).call(() -> {
      INIConfiguration ini = assertHgrc();

      INISection proxy = ini.getSection("http_proxy");
      assertThat(proxy).isNotNull();
      assertThat(proxy.getParameter("host")).isEqualTo("proxy.hitchhiker.org:3128");
      return null;
    });

    INIConfiguration ini = assertHgrc();

    INISection proxy = ini.getSection("http_proxy");
    assertThat(proxy).isNotNull();
    assertThat(proxy.getParameter("host")).isEqualTo("awesome.hitchhiker.com:3128");
  }

  private void writeAuthentication() throws IOException {
    INIConfiguration configuration = new INIConfiguration();
    INISection auth = new INISection("auth");
    auth.setParameter("a.prefix", "awesome.hitchhiker.com");
    auth.setParameter("a.schemes", "ssh");
    auth.setParameter("a.username", "dent");
    auth.setParameter("a.password", "arthur123");
    configuration.addSection(auth);
    INIConfigurationWriter writer = new INIConfigurationWriter();
    writer.write(configuration, hgrc.toFile());
  }

  private void writeProxyConfiguration() throws IOException {
    INIConfiguration configuration = new INIConfiguration();
    INISection proxy = new INISection("http_proxy");
    proxy.setParameter("host", "awesome.hitchhiker.com:3128");
    proxy.setParameter("user", "dent");
    proxy.setParameter("passwd", "arthur123");
    configuration.addSection(proxy);
    INIConfigurationWriter writer = new INIConfigurationWriter();
    writer.write(configuration, hgrc.toFile());
  }


}
