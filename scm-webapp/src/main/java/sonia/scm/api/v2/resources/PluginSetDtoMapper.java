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

package sonia.scm.api.v2.resources;

import jakarta.inject.Inject;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.PluginSet;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class PluginSetDtoMapper {
  private final PluginDtoMapper pluginDtoMapper;

  @Inject
  protected PluginSetDtoMapper(PluginDtoMapper pluginDtoMapper) {
    this.pluginDtoMapper = pluginDtoMapper;
  }

  public List<PluginSetDto> map(Collection<PluginSet> pluginSets, List<AvailablePlugin> availablePlugins, Locale locale) {
    return pluginSets.stream()
      .map(it -> map(it, availablePlugins, locale))
      .sorted(Comparator.comparingInt(PluginSetDto::getSequence))
      .collect(Collectors.toList());
  }

  private PluginSetDto map(PluginSet pluginSet, List<AvailablePlugin> availablePlugins, Locale locale) {
    List<PluginDto> pluginDtos = pluginSet.getPlugins().stream()
      .map(it -> availablePlugins.stream().filter(avail -> avail.getDescriptor().getInformation().getName().equals(it)).findFirst())
      .filter(Optional::isPresent)
      .map(Optional::get)
      .map(pluginDtoMapper::mapAvailable)
      .collect(Collectors.toList());

    PluginSet.Description description = pluginSet.getDescriptions().get(locale.getLanguage());
    if (description == null) {
      description = pluginSet.getDescriptions().get(Locale.ENGLISH.getLanguage());
    }

    return new PluginSetDto(pluginSet.getId(), pluginSet.getSequence(), pluginDtos, description.getName(), description.getFeatures(), pluginSet.getImages());
  }
}
