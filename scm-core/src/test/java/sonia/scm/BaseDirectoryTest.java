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
