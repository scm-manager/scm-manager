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

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import sonia.scm.SCMContextProvider;
import sonia.scm.net.ahc.AdvancedHttpClient;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage") // guava hash is marked as unstable
class PluginInstaller {

  private final SCMContextProvider context;
  private final AdvancedHttpClient client;
  private final SmpDescriptorExtractor smpDescriptorExtractor;

  @Inject
  public PluginInstaller(SCMContextProvider context, AdvancedHttpClient client, SmpDescriptorExtractor smpDescriptorExtractor) {
    this.context = context;
    this.client = client;
    this.smpDescriptorExtractor = smpDescriptorExtractor;
  }

  @SuppressWarnings("squid:S4790") // hashing should be safe
  public PendingPluginInstallation install(AvailablePlugin plugin) {
    Path file = null;
    try (HashingInputStream input = new HashingInputStream(Hashing.sha256(), download(plugin))) {
      file = createFile(plugin);
      Files.copy(input, file);

      verifyChecksum(plugin, input.hash(), file);
      verifyConditions(plugin, file);
      return new PendingPluginInstallation(plugin.install(), file);
    } catch (IOException ex) {
      cleanup(file);
      throw new PluginDownloadException("failed to download plugin", ex);
    }
  }

  private void cleanup(Path file) {
    try {
      if (file != null) {
        Files.deleteIfExists(file);
      }
    } catch (IOException e) {
      throw new PluginInstallException("failed to cleanup, after broken installation");
    }
  }

  private void verifyChecksum(AvailablePlugin plugin, HashCode hash, Path file) {
    Optional<String> checksum = plugin.getDescriptor().getChecksum();
    if (checksum.isPresent()) {
      String calculatedChecksum = hash.toString();
      if (!checksum.get().equalsIgnoreCase(calculatedChecksum)) {
        cleanup(file);
        throw new PluginChecksumMismatchException(
          String.format("downloaded plugin checksum %s does not match expected %s", calculatedChecksum, checksum.get())
        );
      }
    }
  }

  private void verifyConditions(AvailablePlugin plugin, Path file) throws IOException {
    InstalledPluginDescriptor pluginDescriptor = smpDescriptorExtractor.extractPluginDescriptor(file);
    if (!pluginDescriptor.getCondition().isSupported()) {
      cleanup(file);
      throw new PluginConditionFailedException(
        pluginDescriptor.getCondition(),
        String.format(
          "could not load plugin %s, the plugin condition does not match",
          plugin.getDescriptor().getInformation().getName()
        )
      );
    }
  }

  private InputStream download(AvailablePlugin plugin) throws IOException {
    return client.get(plugin.getDescriptor().getUrl()).request().contentAsStream();
  }

  private Path createFile(AvailablePlugin plugin) throws IOException {
    Path directory = context.resolve(Paths.get("plugins"));
    Files.createDirectories(directory);
    return directory.resolve(plugin.getDescriptor().getInformation().getName() + ".smp");
  }
}
