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
    if (Files.exists(file)) {
      try {
        Files.delete(file);
      } catch (IOException ex) {
        throw new PluginFailedToCancelInstallationException("failed to cancel plugin installation ", name, ex);
      }
    } else {
      LOG.info("plugin file {} did not exists for plugin {}; nothing deleted", file, name);
    }
  }
}
