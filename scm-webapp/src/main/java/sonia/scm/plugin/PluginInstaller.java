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

  @Inject
  public PluginInstaller(SCMContextProvider context, AdvancedHttpClient client) {
    this.context = context;
    this.client = client;
  }

  public PendingPluginInstallation install(AvailablePlugin plugin) {
    try (HashingInputStream input = new HashingInputStream(Hashing.sha256(), download(plugin))) {
      Path file = createFile(plugin);
      Files.copy(input, file);

      verifyChecksum(plugin, input.hash());

      // TODO clean up in case of error

      return new PendingPluginInstallation(plugin, file);
    } catch (IOException ex) {
      throw new PluginDownloadException("failed to download plugin", ex);
    }
  }

  private void verifyChecksum(AvailablePlugin plugin, HashCode hash) {
    Optional<String> checksum = plugin.getDescriptor().getChecksum();
    if (checksum.isPresent()) {
      String calculatedChecksum = hash.toString();
      if (!checksum.get().equalsIgnoreCase(calculatedChecksum)) {
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
