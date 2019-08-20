package sonia.scm.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

class PendingPluginInstallation {

  private static final Logger LOG = LoggerFactory.getLogger(PendingPluginInstallation.class);

  private final AvailablePlugin plugin;
  private final File file;

  PendingPluginInstallation(AvailablePlugin plugin, File file) {
    this.plugin = plugin;
    this.file = file;
  }

  public AvailablePlugin getPlugin() {
    return plugin;
  }

  void cancel() {
    String name = plugin.getDescriptor().getInformation().getName();
    LOG.info("cancel installation of plugin {}", name);
    if (!file.delete()) {
      throw new PluginFailedToCancelInstallationException("failed to cancel installation of plugin " + name);
    }
  }
}
