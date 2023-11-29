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
