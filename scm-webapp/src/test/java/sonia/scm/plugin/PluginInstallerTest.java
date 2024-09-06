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
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.util.SystemUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginInstallerTest {

  @Mock
  private SCMContextProvider context;

  @Mock
  private AdvancedHttpClient client;

  @Mock
  private SmpDescriptorExtractor extractor;

  @Mock
  private PluginCenterAuthenticator authenticator;

  @InjectMocks
  private PluginInstaller installer;

  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequest request;

  private Path directory;

  @BeforeEach
  void setUpContext(@TempDir Path directory) throws IOException {
    this.directory = directory;
    lenient().when(context.resolve(any())).then(ic -> {
      Path arg = ic.getArgument(0);
      return directory.resolve(arg);
    });
    InstalledPluginDescriptor supportedPlugin = createPluginDescriptor(
      new PluginCondition("1.0.0", List.of(SystemUtil.getOS()), SystemUtil.getArch())
    );
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
    when(request("https://download.hitchhiker.com").contentAsStream())
      .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
  }

  private AdvancedHttpResponse request(String url) throws IOException {
    AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
    when(client.get(url)).thenReturn(request);
    when(request.request()).thenReturn(response);
    return response;
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
    when(client.get("https://download.hitchhiker.com")).thenReturn(request);
    when(request.request()).thenThrow(new IOException("failed to download"));

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
    when(request("https://download.hitchhiker.com").contentAsStream()).thenReturn(stream);

    PluginInstallationContext context = PluginInstallationContext.empty();
    AvailablePlugin gitPlugin = createGitPlugin();
    assertThrows(PluginDownloadException.class, () -> installer.install(context, gitPlugin));
    assertThat(directory.resolve("plugins").resolve("scm-git-plugin.smp")).doesNotExist();
  }

  @Test
  void shouldFailForUnsupportedPlugin() throws IOException {
    mockContent("42");
    InstalledPluginDescriptor supportedPlugin = createPluginDescriptor(
      new PluginCondition("1.0.0", List.of(SystemUtil.getOS()), "42")
    );
    when(extractor.extractPluginDescriptor(any())).thenReturn(supportedPlugin);

    PluginInstallationContext context = PluginInstallationContext.empty();
    AvailablePlugin gitPlugin = createGitPlugin();
    assertThrows(PluginConditionFailedException.class, () -> installer.install(context, gitPlugin));
    assertThat(directory.resolve("plugins").resolve("scm-git-plugin.smp")).doesNotExist();
  }

  @Test
  void shouldFailForNameMismatch() throws IOException {
    mockContent("42");

    InstalledPluginDescriptor supportedPlugin = createPluginDescriptor("scm-svn-plugin", "1.0.0",
      new PluginCondition("1.0.0", List.of(SystemUtil.getOS()), SystemUtil.getArch())
    );
    when(extractor.extractPluginDescriptor(any())).thenReturn(supportedPlugin);

    PluginInstallationContext context = PluginInstallationContext.empty();
    AvailablePlugin gitPlugin = createGitPlugin();
    PluginInformationMismatchException exception = assertThrows(PluginInformationMismatchException.class, () -> installer.install(context, gitPlugin));
    assertThat(exception.getApi().getName()).isEqualTo("scm-git-plugin");
    assertThat(exception.getDownloaded().getName()).isEqualTo("scm-svn-plugin");
  }

  @Test
  void shouldFailForVersionMismatch() throws IOException {
    mockContent("42");

    InstalledPluginDescriptor supportedPlugin = createPluginDescriptor("scm-git-plugin", "1.1.0",
      new PluginCondition("42.0.0", List.of(SystemUtil.getOS()), SystemUtil.getArch())
    );
    when(extractor.extractPluginDescriptor(any())).thenReturn(supportedPlugin);

    PluginInstallationContext context = PluginInstallationContext.empty();
    AvailablePlugin gitPlugin = createGitPlugin();
    PluginInformationMismatchException exception = assertThrows(PluginInformationMismatchException.class, () -> installer.install(context, gitPlugin));
    assertThat(exception.getApi().getVersion()).isEqualTo("1.0.0");
    assertThat(exception.getDownloaded().getVersion()).isEqualTo("1.1.0");
  }

  @Test
  void shouldAppendBearerAuth() throws IOException {
    when(authenticator.isAuthenticated()).thenReturn(true);
    when(authenticator.fetchAccessToken()).thenReturn(Optional.of("atat"));
    mockContent("42");

    installer.install(PluginInstallationContext.empty(), createGitPlugin());

    verify(request).bearerAuth("atat");
  }

  private AvailablePlugin createPlugin(String name, String url, String checksum) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    information.setVersion("1.0.0");
    AvailablePluginDescriptor descriptor = new AvailablePluginDescriptor(
      information, null, Collections.emptySet(), url, checksum
    );
    return new AvailablePlugin(descriptor);
  }

  private InstalledPluginDescriptor createPluginDescriptor(PluginCondition condition) {
    return createPluginDescriptor("scm-git-plugin", "1.0.0", condition);
  }

  private InstalledPluginDescriptor createPluginDescriptor(String name, String version, PluginCondition condition) {
    InstalledPluginDescriptor installedPluginDescriptor = mock(InstalledPluginDescriptor.class, RETURNS_DEEP_STUBS);
    lenient().when(installedPluginDescriptor.getInformation().getId()).thenReturn(name);
    lenient().when(installedPluginDescriptor.getInformation().getName()).thenReturn(name);
    lenient().when(installedPluginDescriptor.getInformation().getVersion()).thenReturn(version);
    lenient().when(installedPluginDescriptor.getCondition()).thenReturn(condition);
    return installedPluginDescriptor;
  }
}
