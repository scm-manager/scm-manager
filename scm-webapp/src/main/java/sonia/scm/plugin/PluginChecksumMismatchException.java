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

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class PluginChecksumMismatchException extends PluginInstallException {
  public PluginChecksumMismatchException(AvailablePlugin plugin, String calculatedChecksum, String expectedChecksum) {
    super(
      entity("Plugin", plugin.getDescriptor().getInformation().getName()).build(),
      String.format("downloaded plugin checksum %s does not match expected %s", calculatedChecksum, expectedChecksum)
    );
  }

  @Override
  public String getCode() {
    return "6mRuFxaWM1";
  }
}
