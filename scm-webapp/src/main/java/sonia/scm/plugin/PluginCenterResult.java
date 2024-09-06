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

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@AllArgsConstructor
@Getter
class PluginCenterResult {
  private Set<AvailablePlugin> plugins;
  private Set<PluginSet> pluginSets;
  private PluginCenterStatus status;

  public PluginCenterResult() {
    this(Collections.emptySet(), Collections.emptySet(), PluginCenterStatus.OK);
  }

  public PluginCenterResult(PluginCenterStatus status) {
    this(Collections.emptySet(), Collections.emptySet(), status);
  }

  public PluginCenterResult(Set<AvailablePlugin> plugins, Set<PluginSet> pluginSets) {
    this(plugins, pluginSets, PluginCenterStatus.OK);
  }

}
