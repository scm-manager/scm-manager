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
