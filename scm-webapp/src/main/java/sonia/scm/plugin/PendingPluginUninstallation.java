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

  InstalledPlugin getPlugin() {
    return plugin;
  }
}
