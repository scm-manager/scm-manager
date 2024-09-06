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

package sonia.scm.server;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerConfigurationTest {

  @Test
  void shouldThrowServerConfigurationExceptionIfConfigYamlNotFound() {
    assertThrows(ServerConfigurationException.class, () -> new ServerConfiguration(null));
  }

  @Test
  void shouldConfigureServerWithYamlFormat(@TempDir Path directory) throws IOException {
    ServerConfiguration configuration = configure(directory, "default.yml");

    assertThat(configuration.getListeners())
      .hasSize(1)
      .allSatisfy(listener -> {
        assertThat(listener.getScheme()).isEqualTo("http");
        assertThat(listener.getPort()).isEqualTo(8080);
        assertThat(listener.getContextPath()).isEqualTo("/scm");
      });
  }

  @Test
  @SetEnvironmentVariable(key = "SCM_PORT", value = "8081")
  void shouldReturnCustomContextPathAndPort(@TempDir Path directory) throws IOException {
    ServerConfiguration configuration = configure(directory, "ctxPath.yml");

    assertThat(configuration.getListeners())
      .hasSize(1)
      .allSatisfy(listener -> {
        assertThat(listener.getScheme()).isEqualTo("http");
        assertThat(listener.getPort()).isEqualTo(8081);
        assertThat(listener.getContextPath()).isEqualTo("/myscm");
      });
  }

  @Test
  void shouldReturnConfiguredSSListener(@TempDir Path directory) throws IOException {
    ServerConfiguration configuration = configure(directory, "ssl.yml");

    assertThat(configuration.getListeners())
      .hasSize(2)
      .anySatisfy(listener -> {
        assertThat(listener.getScheme()).isEqualTo("http");
        assertThat(listener.getPort()).isEqualTo(80);
        assertThat(listener.getContextPath()).isEqualTo("/scm");
      })
      .anySatisfy(listener -> {
        assertThat(listener.getScheme()).isEqualTo("https");
        assertThat(listener.getPort()).isEqualTo(443);
        assertThat(listener.getContextPath()).isEqualTo("/scm");
      });
    ;
  }

  @SuppressWarnings("UnstableApiUsage")
  private ServerConfiguration configure(Path tempDir, String configurationFilename) throws IOException {
    System.setProperty("basedir", tempDir.toString());

    URL resource = Resources.getResource("sonia/scm/server/" + configurationFilename);
    Path path = tempDir.resolve("config.yml");

    Files.write(path, Resources.toByteArray(resource));
    Files.createDirectories(tempDir.resolve("var/webapp/docroot"));
    Files.createFile(tempDir.resolve("var/webapp/scm-webapp.war"));

    System.setProperty("basedir", tempDir.toString());
    return new ServerConfiguration(path.toUri().toURL());
  }

}
