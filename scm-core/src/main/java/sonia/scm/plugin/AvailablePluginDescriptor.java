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

import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * @since 2.0.0
 */
public class AvailablePluginDescriptor implements PluginDescriptor {

  private final PluginInformation information;
  private final PluginCondition condition;
  private final Set<String> dependencies;
  private final Set<String> optionalDependencies;
  private final String url;
  private final String checksum;

  /**
   * @deprecated Use {@link #AvailablePluginDescriptor(PluginInformation, PluginCondition, Set, Set, String, String)} instead
   */
  @Deprecated
  public AvailablePluginDescriptor(PluginInformation information, PluginCondition condition, Set<String> dependencies, String url, String checksum) {
    this(information, condition, dependencies, emptySet(), url, checksum);
  }

  public AvailablePluginDescriptor(PluginInformation information, PluginCondition condition, Set<String> dependencies, Set<String> optionalDependencies, String url, String checksum) {
    this.information = information;
    this.condition = condition;
    this.dependencies = dependencies;
    this.optionalDependencies = optionalDependencies;
    this.url = url;
    this.checksum = checksum;
  }

  public String getUrl() {
    return url;
  }

  public Optional<String> getChecksum() {
    return Optional.ofNullable(checksum);
  }

  @Override
  public PluginInformation getInformation() {
    return information;
  }

  @Override
  public PluginCondition getCondition() {
    return condition;
  }

  @Override
  public Set<String> getDependencies() {
    return dependencies;
  }

  @Override
  public Set<String> getOptionalDependencies() {
    return optionalDependencies;
  }
}
