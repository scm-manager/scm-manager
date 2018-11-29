package sonia.scm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TempDirectory.class)
class BasicContextProviderTest {

  private Path baseDirectory;

  private BasicContextProvider context;

  @BeforeEach
  void setUpContext(@TempDirectory.TempDir Path baseDirectory) {
    this.baseDirectory = baseDirectory;
    context = new BasicContextProvider(baseDirectory.toFile(), "x.y.z", Stage.PRODUCTION);
  }

  @Test
  void shouldReturnAbsolutePathAsIs(@TempDirectory.TempDir Path path) {
    Path absolutePath = path.toAbsolutePath();
    Path resolved = context.resolve(absolutePath);

    assertThat(resolved).isSameAs(absolutePath);
  }

  @Test
  void shouldResolveRelatePath() {
    Path path = Paths.get("repos", "42");
    Path resolved = context.resolve(path);

    assertThat(resolved).isAbsolute();
    assertThat(resolved).startsWithRaw(baseDirectory);
    assertThat(resolved).endsWithRaw(path);
  }

}
