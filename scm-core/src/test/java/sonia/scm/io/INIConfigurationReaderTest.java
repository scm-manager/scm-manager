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
