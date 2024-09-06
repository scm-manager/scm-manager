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

/**
 * @since 1.21
 */
public final class PluginConditionFailedException extends PluginException {

  private final PluginCondition condition;
  private final String pluginName;

  private static final long serialVersionUID = 3257937172323964102L;


  public PluginConditionFailedException(String pluginName, PluginCondition condition) {
    super(String.format(
      "could not load plugin %s, the plugin condition does not match: %s",
      pluginName, condition
    ));
    this.pluginName = pluginName;
    this.condition = condition;
  }

  public String getPluginName() {
    return pluginName;
  }

  public PluginCondition getCondition() {
    return condition;
  }
}
