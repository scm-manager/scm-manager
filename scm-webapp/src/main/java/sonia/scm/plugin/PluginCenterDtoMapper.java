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

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public abstract class PluginCenterDtoMapper {

  PluginCenterDtoMapper() {}

  static final PluginCenterDtoMapper INSTANCE = Mappers.getMapper(PluginCenterDtoMapper.class);

  abstract PluginInformation map(PluginCenterDto.Plugin plugin);

  abstract PluginCondition map(PluginCenterDto.Condition condition);

  abstract PluginSet map(PluginCenterDto.PluginSet set);
  abstract PluginSet.Description map(PluginCenterDto.Description description);

  PluginCenterResult map(PluginCenterDto pluginCenterDto) {
    Set<AvailablePlugin> plugins = new HashSet<>();
    Set<PluginSet> pluginSets = pluginCenterDto
      .getEmbedded()
      .getPluginSets()
      .stream()
      .map(this::map)
      .collect(Collectors.toSet());

    for (PluginCenterDto.Plugin plugin : pluginCenterDto.getEmbedded().getPlugins()) {
      String url = plugin.getLinks().get("download").getHref();
      AvailablePluginDescriptor descriptor = new AvailablePluginDescriptor(
        map(plugin), map(plugin.getConditions()), plugin.getDependencies(), plugin.getOptionalDependencies(), url, plugin.getSha256sum()
      );
      plugins.add(new AvailablePlugin(descriptor));
    }
    return new PluginCenterResult(plugins, pluginSets);
  }
}
