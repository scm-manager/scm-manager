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

package sonia.scm;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.config.WebappConfigProvider;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class BaseDirectoryTest {

  @AfterEach
  void clearConfig() {
    WebappConfigProvider.setConfigBindings(emptyMap());
  }

  @Test
  void shouldGetFromClassPathResource() {
    BaseDirectory directory = builder().withClassPathResource("/sonia/scm/basedirectory.properties").create();
    assertThat(directory.find()).isEqualTo(Paths.get("/tmp/scm_home"));
  }

  @Test
  void shouldGetFromEnvironmentVariable() {
    WebappConfigProvider.setConfigBindings(Map.of("homeDir",  "/tmp/scm_home"));
    BaseDirectory directory = builder().create();
    assertThat(directory.find()).isEqualTo(Paths.get("/tmp/scm_home"));
  }

  @Nested
  class OsSpecificDefaults {

    @Test
    void linux() {
      BaseDirectory directory = builder().withSystemProperty("user.home", "/tmp").create();
      assertThat(directory.find()).isEqualTo(Paths.get("/tmp/.scm"));
    }

    @Test
    void osx() {
      BaseDirectory directory = builder().withOsx().withSystemProperty("user.home", "/tmp").create();
      assertThat(directory.find()).isEqualTo(Paths.get("/tmp/Library/Application Support/SCM-Manager"));
    }

    @Test
    void windows() {
      BaseDirectory directory = builder().withWindows().withEnvironment("APPDATA", "/tmp").create();
      assertThat(directory.find()).isEqualTo(Paths.get("/tmp\\SCM-Manager"));
    }

  }

  private BaseDirectoryBuilder builder() {
    return new BaseDirectoryBuilder();
  }

  static class BaseDirectoryBuilder {

    private Platform platform = platform("Linux");
    private String classPathResource = "/scm.properties";
    private Map<String, String> environment = ImmutableMap.of();
    private Properties systemProperties = new Properties();

    public BaseDirectoryBuilder withOsx() {
      this.platform = platform("Mac OS X");
      return this;
    }

    public BaseDirectoryBuilder withWindows() {
      this.platform = platform("Windows");
      return this;
    }

    private Platform platform(String osName) {
      return new Platform(osName, "64", "x86_64");
    }

    public BaseDirectoryBuilder withClassPathResource(String classPathResource) {
      this.classPathResource = classPathResource;
      return this;
    }

    public BaseDirectoryBuilder withEnvironment(String key, String value) {
      this.environment = ImmutableMap.of(key, value);
      return this;
    }

    public BaseDirectoryBuilder withSystemProperty(String key, String value) {
      systemProperties.put(key, value);
      return this;
    }

    public BaseDirectory create() {
      return new BaseDirectory(platform, classPathResource, environment, systemProperties);
    }
  }

}
