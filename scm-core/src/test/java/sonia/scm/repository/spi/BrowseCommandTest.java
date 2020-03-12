package sonia.scm.repository.spi;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.BrowserResult;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.spi.BrowseCommandTest.Entry.d;
import static sonia.scm.repository.spi.BrowseCommandTest.Entry.f;

class BrowseCommandTest implements BrowseCommand {

  @Test
  void shouldSort() {
    List<Entry> entries = asList(
      f("b.txt"),
      f("a.txt"),
      f("Dockerfile"),
      f(".gitignore"),
      d("src"),
      f("README")
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

    static Entry f(String name) {
      return new Entry(name, false);
    }

    static Entry d(String name) {
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
