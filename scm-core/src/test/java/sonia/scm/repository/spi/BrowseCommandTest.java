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
