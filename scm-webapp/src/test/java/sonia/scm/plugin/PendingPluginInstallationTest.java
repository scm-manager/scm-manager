package sonia.scm.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, TempDirectory.class})
class PendingPluginInstallationTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AvailablePlugin plugin;

  @Test
  void shouldDeleteFileOnCancel(@TempDirectory.TempDir Path directory) throws IOException {
    Path file = directory.resolve("file");
    Files.write(file, "42".getBytes());

    when(plugin.getDescriptor().getInformation().getName()).thenReturn("scm-awesome-plugin");

    PendingPluginInstallation installation = new PendingPluginInstallation(plugin, file);
    installation.cancel();

    assertThat(file).doesNotExist();
  }

  @Test
  void shouldThrowExceptionIfCancelFailed(@TempDirectory.TempDir Path directory) {
    Path file = directory.resolve("file");
    when(plugin.getDescriptor().getInformation().getName()).thenReturn("scm-awesome-plugin");

    PendingPluginInstallation installation = new PendingPluginInstallation(plugin, file);
    assertThrows(PluginFailedToCancelInstallationException.class, installation::cancel);
  }

}
