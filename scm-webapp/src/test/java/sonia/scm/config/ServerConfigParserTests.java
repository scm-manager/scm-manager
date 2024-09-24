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

package sonia.scm.config;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ServerConfigParserTests {

  @Test
  void shouldApplyConfigurationFromResourceFile() {
    ServerConfigParser parser = new ServerConfigParser();
    ServerConfigYaml config = parser.parse(Resources.getResource("sonia/scm/config/config.yml"));

    assertThat(config.getLog().getLogDir()).isEqualTo("/etc/scm/logs");
    assertThat(config.getLog().getRootLevel()).isEqualTo("WARN");
    assertThat(config.getLog().isConsoleAppenderEnabled()).isFalse();
    assertThat(config.getLog().isFileAppenderEnabled()).isFalse();
    assertThat(config.getLog().getLogger()).isEqualTo(Map.of("sonia.scm", "ERROR"));

    assertThat(config.getWebapp()).isEqualTo(Map.of(
            "workDir", "/tmp/work/dir",
            "homeDir", "/home/dir",
            "cache", Map.of("dataFile", Map.of("enabled", true), "store", Map.of("enabled", true)),
            "endlessJwt", true,
            "asyncThreads", 8,
            "maxAsyncAbortSeconds", 120,
            "centralWorkQueue", Map.of("workers", 8),
            "workingCopyPoolStrategy", "copyPoolStrategy",
            "workingCopyPoolSize", 12
    ));
  }

  @Test
  @SetEnvironmentVariable(key = "SCM_LOG_DIR", value = "/ENV/LOG/DIR")
  @SetEnvironmentVariable(key = "SCM_LOG_ROOT_LEVEL", value = "ENV_WARN")
  @SetEnvironmentVariable(key = "SCM_LOG_FILE_APPENDER_ENABLED", value = "true")
  @SetEnvironmentVariable(key = "SCM_LOG_CONSOLE_APPENDER_ENABLED", value = "true")
  @SetEnvironmentVariable(key = "SCM_LOG_LOGGER", value = "sonia.env:ENV_INFO")
  void shouldOverrideConfigFileWithEnvironmentVariables() {
    ServerConfigParser parser = new ServerConfigParser();
    ServerConfigYaml config = parser.parse(Resources.getResource("sonia/scm/config/config.yml"));

    assertThat(config.getLog().getLogDir()).isEqualTo("/ENV/LOG/DIR");
    assertThat(config.getLog().getRootLevel()).isEqualTo("ENV_WARN");
    assertThat(config.getLog().isConsoleAppenderEnabled()).isTrue();
    assertThat(config.getLog().isFileAppenderEnabled()).isTrue();
    assertThat(config.getLog().getLogger()).isEqualTo(Map.of("sonia.env", "ENV_INFO"));
  }
}
