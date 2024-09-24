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

package sonia.scm.repository.spi;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.BrowserResult;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.spi.BrowseCommandTest.Entry.directory;
import static sonia.scm.repository.spi.BrowseCommandTest.Entry.file;

class BrowseCommandTest implements BrowseCommand {

  @Test
  void shouldSort() {
    List<Entry> entries = asList(
      file("b.txt"),
      file("a.txt"),
      file("Dockerfile"),
      file(".gitignore"),
      directory("src"),
      file("README")
    );

    sort(entries, Entry::isDirectory, Entry::getName);

    assertThat(entries).extracting("name")
      .containsExactly(
        "src",
        ".gitignore",
        "Dockerfile",
        "README",
        "a.txt",
        "b.txt"
      );
  }

  @Override
  public BrowserResult getBrowserResult(BrowseCommandRequest request) throws IOException {
    return null;
  }

  static class Entry {
    private final String name;
    private final boolean directory;

    static Entry file(String name) {
      return new Entry(name, false);
    }

    static Entry directory(String name) {
      return new Entry(name, true);
    }

    public Entry(String name, boolean directory) {
      this.name = name;
      this.directory = directory;
    }

    public String getName() {
      return name;
    }

    public boolean isDirectory() {
      return directory;
    }
  }
}
