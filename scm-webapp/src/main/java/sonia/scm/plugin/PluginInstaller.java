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
  private final SmpDDescriptorExtractor smpDDescriptorExtractor;

  @Inject
  public PluginInstaller(SCMContextProvider context, AdvancedHttpClient client, SmpDDescriptorExtractor smpDDescriptorExtractor) {
    this.context = context;
    this.client = client;
    this.smpDDescriptorExtractor = smpDDescriptorExtractor;
  }

  @SuppressWarnings("squid:S4790") // hashing should be safe
  public PendingPluginInstallation install(AvailablePlugin plugin) {
    Path file = null;
    try (HashingInputStream input = new HashingInputStream(Hashing.sha256(), download(plugin))) {
      file = createFile(plugin);
      Files.copy(input, file);

      verifyChecksum(plugin, input.hash(), file);
      InstalledPluginDescriptor pluginDescriptor = smpDDescriptorExtractor.extractPluginDescriptor(file);
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

  private InputStream download(AvailablePlugin plugin) throws IOException {
    return client.get(plugin.getDescriptor().getUrl()).request().contentAsStream();
  }

  private Path createFile(AvailablePlugin plugin) throws IOException {
    Path directory = context.resolve(Paths.get("plugins"));
    Files.createDirectories(directory);
    return directory.resolve(plugin.getDescriptor().getInformation().getName() + ".smp");
  }
}
