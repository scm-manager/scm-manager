package sonia.scm.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class PendingPluginUninstallation {

  private static final Logger LOG = LoggerFactory.getLogger(PendingPluginUninstallation.class);

  private final InstalledPlugin plugin;
  private final Path uninstallFile;

  PendingPluginUninstallation(InstalledPlugin plugin, Path uninstallFile) {
    this.plugin = plugin;
    this.uninstallFile = uninstallFile;
  }

  void cancel() {
    String name = plugin.getDescriptor().getInformation().getName();
    LOG.info("cancel uninstallation of plugin {}", name);
    try {
      Files.delete(uninstallFile);
      plugin.setMarkedForUninstall(false);
    } catch (IOException ex) {
      throw new PluginFailedToCancelInstallationException("failed to cancel uninstallation", name, ex);
    }
  }
}
