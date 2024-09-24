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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.Platform;
import sonia.scm.SCMContextProvider;

import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class ScmLogFilePropertyDefinerTest {

  @Mock
  private SCMContextProvider context;

  @Test
  void shouldReturnPath(@TempDir Path tempDir) {
    when(context.getBaseDirectory()).thenReturn(tempDir.toFile());
    ScmLogFilePropertyDefiner definer = builder().create();

    Path logDirectory = tempDir.resolve("logs");
    assertThat(definer.getPropertyValue()).isEqualTo(logDirectory.toAbsolutePath().toString());
  }

  @Test
  void shouldReturnOsxPath(@TempDir Path tempDir) {
    ScmLogFilePropertyDefiner definer = builder()
      .withOs("Mac OS X")
      .withUserHome(tempDir.toAbsolutePath().toString())
      .create();

    Path logDirectory = tempDir.resolve("Library/Logs/SCM-Manager");
    assertThat(definer.getPropertyValue()).isEqualTo(logDirectory.toAbsolutePath().toString());
  }

  private ScmLogFilePropertyDefinerBuilder builder() {
    return new ScmLogFilePropertyDefinerBuilder();
  }

  private class ScmLogFilePropertyDefinerBuilder {

    private Platform platform;
    private Properties properties = new Properties();

    public ScmLogFilePropertyDefinerBuilder() {
      withOs("Linux");
    }

    public ScmLogFilePropertyDefinerBuilder withOs(String osName) {
      platform = new Platform(osName, "64", "x86_64");
      return this;
    }

    public ScmLogFilePropertyDefinerBuilder withUserHome(String path) {
      properties.setProperty("user.home", path);
      return this;
    }

    public ScmLogFilePropertyDefiner create() {
      return new ScmLogFilePropertyDefiner(context, platform, properties);
    }

  }
}
