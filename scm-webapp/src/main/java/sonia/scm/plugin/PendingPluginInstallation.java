package sonia.scm.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class PendingPluginInstallation {

  private static final Logger LOG = LoggerFactory.getLogger(PendingPluginInstallation.class);

  private final AvailablePlugin plugin;
  private final Path file;

  PendingPluginInstallation(AvailablePlugin plugin, Path file) {
    this.plugin = plugin;
    this.file = file;
  }

  public AvailablePlugin getPlugin() {
    return plugin;
  }

  void cancel() {
    String name = plugin.getDescriptor().getInformation().getName();
    LOG.info("cancel installation of plugin {}", name);
    try {
      Files.delete(file);
    } catch (IOException ex) {
      throw new PluginFailedToCancelInstallationException("failed to cancel installation of plugin " + name, ex);
    }
  }
}
