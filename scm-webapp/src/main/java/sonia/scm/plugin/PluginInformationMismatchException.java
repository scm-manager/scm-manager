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

import lombok.Getter;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Getter
@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class PluginInformationMismatchException extends PluginInstallException {

  private final PluginInformation api;
  private final PluginInformation downloaded;

  public PluginInformationMismatchException(PluginInformation api, PluginInformation downloaded, String message) {
    super(
      entity("Plugin", api.getName()).build(),
      message
    );
    this.api = api;
    this.downloaded = downloaded;
  }

  @Override
  public String getCode() {
    return "4RS6niPRX1";
  }
}
