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

package sonia.scm.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class INIConfigurationReaderTest {

  private final INIConfigurationReader reader = new INIConfigurationReader();

  @Test
  void shouldReadIni(@TempDir Path directory) throws IOException {
    String ini = String.join(System.getProperty("line.separator"),
      "[one]",
      "a = b",
      "[two]",
      "c = d",
      "",
      "[three]",
      "e = f",
      "",
      ""
    );

    Path file = directory.resolve("config.ini");
    Files.write(file, ini.getBytes(StandardCharsets.UTF_8));

    INIConfiguration configuration = reader.read(file.toFile());

    INISection one = configuration.getSection("one");
    assertThat(one).isNotNull();
    assertThat(one.getParameter("a")).isEqualTo("b");

    INISection two = configuration.getSection("two");
    assertThat(two).isNotNull();
    assertThat(two.getParameter("c")).isEqualTo("d");

    INISection three = configuration.getSection("three");
    assertThat(three).isNotNull();
    assertThat(three.getParameter("e")).isEqualTo("f");
  }

}
