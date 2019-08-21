package sonia.scm.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.net.ahc.AdvancedHttpClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, TempDirectory.class})
class PluginInstallerTest {

  @Mock
  private SCMContextProvider context;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AdvancedHttpClient client;

  @InjectMocks
  private PluginInstaller installer;

  private Path directory;

  @BeforeEach
  void setUpContext(@TempDirectory.TempDir Path directory) {
    this.directory = directory;
    lenient().when(context.resolve(any())).then(ic -> {
      Path arg = ic.getArgument(0);
      return directory.resolve(arg);
    });
  }

  @Test
  void shouldDownloadPlugin() throws IOException {
    mockContent("42");

    installer.install(createGitPlugin());

    assertThat(directory.resolve("plugins").resolve("scm-git-plugin.smp")).hasContent("42");
  }

  @Test
  void shouldReturnPendingPluginInstallation() throws IOException {
    mockContent("42");
    AvailablePlugin gitPlugin = createGitPlugin();

    PendingPluginInstallation pending = installer.install(gitPlugin);

    assertThat(pending).isNotNull();
    assertThat(pending.getPlugin()).isSameAs(gitPlugin);
  }

  private void mockContent(String content) throws IOException {
    when(client.get("https://download.hitchhiker.com").request().contentAsStream())
      .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
  }

  private AvailablePlugin createGitPlugin() {
    return createPlugin(
      "scm-git-plugin",
      "https://download.hitchhiker.com",
      "73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049" // 42
    );
  }

  @Test
  void shouldThrowPluginDownloadException() throws IOException {
    when(client.get("https://download.hitchhiker.com").request()).thenThrow(new IOException("failed to download"));

    assertThrows(PluginDownloadException.class, () -> installer.install(createGitPlugin()));
  }

  @Test
  void shouldThrowPluginChecksumMismatchException() throws IOException {
    mockContent("21");

    assertThrows(PluginChecksumMismatchException.class, () -> installer.install(createGitPlugin()));
  }


  private AvailablePlugin createPlugin(String name, String url, String checksum) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    AvailablePluginDescriptor descriptor = new AvailablePluginDescriptor(
      information, null, Collections.emptySet(), url, checksum
    );
    return new AvailablePlugin(descriptor);
  }
}
