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

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.PluginCenterStatus;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginPermissions;

import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class PluginDtoCollectionMapper {

  private final ResourceLinks resourceLinks;
  private final PluginDtoMapper mapper;
  private final PluginManager manager;

  @Inject
  public PluginDtoCollectionMapper(ResourceLinks resourceLinks, PluginDtoMapper mapper, PluginManager manager) {
    this.resourceLinks = resourceLinks;
    this.mapper = mapper;
    this.manager = manager;
  }

  public PluginCollectionDto mapInstalled(PluginManager.PluginResult plugins) {
    List<PluginDto> dtos = plugins
      .getInstalledPlugins()
      .stream()
      .map(i -> mapper.mapInstalled(i, plugins.getAvailablePlugins()))
      .collect(toList());
    return new PluginCollectionDto(createInstalledPluginsLinks(), embedDtos(dtos), plugins.getPluginCenterStatus());
  }

  public PluginCollectionDto mapAvailable(List<AvailablePlugin> plugins, PluginCenterStatus pluginCenterStatus) {
    List<PluginDto> dtos = plugins.stream().map(mapper::mapAvailable).collect(toList());
    return new PluginCollectionDto(createAvailablePluginsLinks(plugins), embedDtos(dtos), pluginCenterStatus);
  }

  private Links createInstalledPluginsLinks() {
    String baseUrl = resourceLinks.installedPluginCollection().self();

    Links.Builder linksBuilder = linkingTo()
      .with(Links.linkingTo().self(baseUrl).build());

    if (!manager.getUpdatable().isEmpty() && PluginPermissions.write().isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.installedPluginCollection().update()));
    }

    return linksBuilder.build();
  }

  private Links createAvailablePluginsLinks(List<AvailablePlugin> plugins) {
    String baseUrl = resourceLinks.availablePluginCollection().self();

    Links.Builder linksBuilder = linkingTo()
      .with(Links.linkingTo().self(baseUrl).build());

    return linksBuilder.build();
  }

  private boolean containsPending(List<AvailablePlugin> plugins) {
    return plugins.stream().anyMatch(AvailablePlugin::isPending);
  }

  private Embedded embedDtos(List<PluginDto> dtos) {
    return embeddedBuilder()
      .with("plugins", dtos)
      .build();
  }
}
