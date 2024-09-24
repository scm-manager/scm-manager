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
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class INIConfigurationWriterTest {

  @Test
  void shouldWriteIni(@TempDir Path directory) throws IOException {
    Path path = directory.resolve("config.ini");

    INIConfiguration configuration = new INIConfiguration();

    INISection one = new INISection("one");
    one.setParameter("a", "b");
    configuration.addSection(one);

    INISection two = new INISection("two");
    two.setParameter("c", "d");
    configuration.addSection(two);

    INIConfigurationWriter writer = new INIConfigurationWriter();
    writer.write(configuration, path.toFile());

    String expected = String.join(System.getProperty("line.separator"),
      "[one]",
      "a = b",
      "",
      "[two]",
      "c = d",
      "",
      ""
    );
    assertThat(path).hasContent(expected);
  }

}
