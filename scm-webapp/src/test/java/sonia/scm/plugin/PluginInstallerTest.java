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

package sonia.scm.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.net.ahc.AdvancedHttpClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class PluginInstallerTest {

  @Mock
  private SCMContextProvider context;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AdvancedHttpClient client;

  @Mock
  private SmpDescriptorExtractor extractor;

  @InjectMocks
  private PluginInstaller installer;

  private Path directory;

  @BeforeEach
  void setUpContext(@TempDir Path directory) throws IOException {
    this.directory = directory;
    lenient().when(context.resolve(any())).then(ic -> {
      Path arg = ic.getArgument(0);
      return directory.resolve(arg);
    });
    InstalledPluginDescriptor supportedPlugin = createPluginDescriptor(true);
    lenient().when(extractor.extractPluginDescriptor(any())).thenReturn(supportedPlugin);
  }

  @Test
  void shouldDownloadPlugin() throws IOException {
    mockContent("42");

    installer.install(PluginInstallationContext.empty(), createGitPlugin());

    assertThat(directory.resolve("plugins").resolve("scm-git-plugin.smp")).hasContent("42");
  }

  @Test
  void shouldReturnPendingPluginInstallation() throws IOException {
    mockContent("42");
    AvailablePlugin gitPlugin = createGitPlugin();

    PendingPluginInstallation pending = installer.install(PluginInstallationContext.empty(), gitPlugin);

    assertThat(pending).isNotNull();
    assertThat(pending.getPlugin().getDescriptor()).isEqualTo(gitPlugin.getDescriptor());
    assertThat(pending.getPlugin().isPending()).isTrue();
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

    PluginInstallationContext context = PluginInstallationContext.empty();
    AvailablePlugin gitPlugin = createGitPlugin();
    assertThrows(PluginDownloadException.class, () -> installer.install(context, gitPlugin));
  }

  @Test
  void shouldThrowPluginChecksumMismatchException() throws IOException {
    mockContent("21");

    PluginInstallationContext context = PluginInstallationContext.empty();
    AvailablePlugin gitPlugin = createGitPlugin();
    assertThrows(PluginChecksumMismatchException.class, () -> installer.install(context, gitPlugin));
    assertThat(directory.resolve("plugins").resolve("scm-git-plugin.smp")).doesNotExist();
  }

  @Test
  void shouldThrowPluginDownloadExceptionAndCleanup() throws IOException {
    InputStream stream = mock(InputStream.class);
    when(stream.read(any(), anyInt(), anyInt())).thenThrow(new IOException("failed to read"));
    when(client.get("https://download.hitchhiker.com").request().contentAsStream()).thenReturn(stream);

    PluginInstallationContext context = PluginInstallationContext.empty();
    AvailablePlugin gitPlugin = createGitPlugin();
    assertThrows(PluginDownloadException.class, () -> installer.install(context, gitPlugin));
    assertThat(directory.resolve("plugins").resolve("scm-git-plugin.smp")).doesNotExist();
  }

  @Test
  void shouldFailForUnsupportedPlugin() throws IOException {
    mockContent("42");
    InstalledPluginDescriptor supportedPlugin = createPluginDescriptor(false);
    when(extractor.extractPluginDescriptor(any())).thenReturn(supportedPlugin);

    PluginInstallationContext context = PluginInstallationContext.empty();
    AvailablePlugin gitPlugin = createGitPlugin();
    assertThrows(PluginConditionFailedException.class, () -> installer.install(context, gitPlugin));
    assertThat(directory.resolve("plugins").resolve("scm-git-plugin.smp")).doesNotExist();
  }

  private AvailablePlugin createPlugin(String name, String url, String checksum) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    AvailablePluginDescriptor descriptor = new AvailablePluginDescriptor(
      information, null, Collections.emptySet(), url, checksum
    );
    return new AvailablePlugin(descriptor);
  }

  private InstalledPluginDescriptor createPluginDescriptor(boolean supported) {
    InstalledPluginDescriptor installedPluginDescriptor = mock(InstalledPluginDescriptor.class, RETURNS_DEEP_STUBS);
    lenient().when(installedPluginDescriptor.getCondition().isSupported()).thenReturn(supported);
    lenient().when(installedPluginDescriptor.getInformation().getId()).thenReturn("scm-git-plugin");
    return installedPluginDescriptor;
  }
}
