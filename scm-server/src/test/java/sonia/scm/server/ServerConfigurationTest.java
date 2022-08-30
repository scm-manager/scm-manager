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

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ClearSystemProperty(key = "basedir")
class ServerConfigurationTest {

  @Test
  void shouldReturnDefaultListener(@TempDir Path directory) throws IOException {
    ServerConfiguration configuration = configure(directory, "default.xml");

    assertThat(configuration.getListeners())
      .hasSize(1)
      .allSatisfy(listener -> {
        assertThat(listener.getScheme()).isEqualTo("http");
        assertThat(listener.getPort()).isEqualTo(8080);
        assertThat(listener.getContextPath()).isEqualTo("/scm");
      });
  }

  @Test
  @SetSystemProperty(key = "jetty.port", value = "8081")
  void shouldReturnCustomContextPathAndPort(@TempDir Path directory) throws IOException {
    ServerConfiguration configuration = configure(directory, "ctxPath.xml");

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
    ServerConfiguration configuration = configure(directory, "ssl.xml");

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
  private ServerConfiguration configure(Path directory, String configurationFilename) throws IOException {
    URL resource = Resources.getResource("sonia/scm/server/" + configurationFilename);
    Path path = directory.resolve("server-config.xml");

    Files.write(path, Resources.toByteArray(resource));
    Files.createDirectories(directory.resolve("var/webapp/docroot"));
    Files.createFile(directory.resolve("var/webapp/scm-webapp.war"));

    System.setProperty("basedir", directory.toString());

    return new ServerConfiguration(path);
  }

}
