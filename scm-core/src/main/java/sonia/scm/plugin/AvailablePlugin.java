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

import com.google.common.base.Preconditions;

public class AvailablePlugin implements Plugin {

  private final AvailablePluginDescriptor pluginDescriptor;
  private final boolean pending;

  public AvailablePlugin(AvailablePluginDescriptor pluginDescriptor) {
    this(pluginDescriptor, false);
  }

  private AvailablePlugin(AvailablePluginDescriptor pluginDescriptor, boolean pending) {
    this.pluginDescriptor = pluginDescriptor;
    this.pending = pending;
  }

  @Override
  public AvailablePluginDescriptor getDescriptor() {
    return pluginDescriptor;
  }

  public boolean isPending() {
    return pending;
  }

  AvailablePlugin install() {
    Preconditions.checkState(!pending, "installation is already pending");
    return new AvailablePlugin(pluginDescriptor, true);
  }
}
