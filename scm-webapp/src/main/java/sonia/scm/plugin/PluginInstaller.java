package sonia.scm.plugin;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import sonia.scm.SCMContextProvider;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

class PluginInstaller {

  private final SCMContextProvider context;
  private final AdvancedHttpClient client;

  @Inject
  public PluginInstaller(SCMContextProvider context, AdvancedHttpClient client) {
    this.context = context;
    this.client = client;
  }

  public PendingPluginInstallation install(AvailablePlugin plugin) {
    File file = createFile(plugin);
    try (InputStream input = download(plugin); OutputStream output = new FileOutputStream(file)) {
      ByteStreams.copy(input, output);

      verifyChecksum(plugin, file);

      // TODO clean up in case of error

      return new PendingPluginInstallation(plugin, file);
    } catch (IOException ex) {
      throw new PluginDownloadException("failed to install plugin", ex);
    }
  }

  private void verifyChecksum(AvailablePlugin plugin, File file) throws IOException {
    Optional<String> checksum = plugin.getDescriptor().getChecksum();
    if (checksum.isPresent()) {
      String calculatedChecksum = Files.hash(file, Hashing.sha256()).toString();
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

  private File createFile(AvailablePlugin plugin) {
    File pluginDirectory = new File(context.getBaseDirectory(), "plugins");
    IOUtil.mkdirs(pluginDirectory);
    return new File(pluginDirectory, plugin.getDescriptor().getInformation().getName() + ".smp");
  }
}
