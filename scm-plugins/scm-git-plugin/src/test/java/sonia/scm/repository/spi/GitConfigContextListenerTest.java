package sonia.scm.repository.spi;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.util.SystemReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class GitConfigContextListenerTest {

  @Test
  void shouldSetGitConfig() throws IOException, ConfigInvalidException {
    System.setProperty("scm.git.core.someTestKey", "testValue");
    new GitConfigContextListener().contextInitialized(null);
    assertThat(
      SystemReader.getInstance().getSystemConfig().getString("core", null, "someTestKey")
    ).isEqualTo("testValue");
  }
}
