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

import com.google.common.base.Strings;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginPermissions;

import java.util.List;
import java.util.Optional;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class PluginDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Inject
  private Restarter restarter;

  @Mapping(target = "newVersion", ignore = true)
  @Mapping(target = "pending", ignore = true)
  @Mapping(target = "core", ignore = true)
  @Mapping(target = "markedForUninstall", ignore = true)
  @Mapping(target = "dependencies", ignore = true)
  @Mapping(target = "optionalDependencies", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  public abstract void map(PluginInformation plugin, @MappingTarget PluginDto dto);

  public PluginDto mapInstalled(InstalledPlugin plugin, List<AvailablePlugin> availablePlugins) {
    PluginDto dto = createDtoForInstalled(plugin, availablePlugins);
    map(dto, plugin);
    return dto;
  }

  public PluginDto mapAvailable(AvailablePlugin plugin) {
    PluginDto dto = createDtoForAvailable(plugin);
    map(dto, plugin);
    dto.setPending(plugin.isPending());
    if (dto.getType() == null) {
      dto.setType(PluginInformation.PluginType.SCM);
    }
    return dto;
  }

  private void map(PluginDto dto, Plugin plugin) {
    dto.setDependencies(plugin.getDescriptor().getDependencies());
    dto.setOptionalDependencies(plugin.getDescriptor().getOptionalDependencies());
    map(plugin.getDescriptor().getInformation(), dto);
    if (dto.getCategory() == null) {
      dto.setCategory("Miscellaneous");
    }
  }

  private PluginDto createDtoForAvailable(AvailablePlugin plugin) {
    PluginInformation information = plugin.getDescriptor().getInformation();

    Links.Builder links = linkingTo()
      .self(resourceLinks.availablePlugin()
        .self(information.getName()));

    if (!plugin.isPending() && PluginPermissions.write().isPermitted()) {
      boolean isCloudoguPlugin = plugin.getDescriptor().getInformation().getType() == PluginInformation.PluginType.CLOUDOGU;
      if (isCloudoguPlugin) {
        Optional<String> cloudoguInstallLink = plugin.getDescriptor().getInstallLink();
        cloudoguInstallLink.ifPresent(link -> links.single(link("cloudoguInstall", link)));
      }

      if (!Strings.isNullOrEmpty(plugin.getDescriptor().getUrl())) {
        String href = resourceLinks.availablePlugin().install(information.getName());
        appendLink(links, "install", href);
      }
    }

    return new PluginDto(links.build());
  }

  private void appendLink(Links.Builder links, String name, String href) {
    links.single(link(name, href));
    if (restarter.isSupported()) {
      links.single(link(name + "WithRestart", href + "?restart=true"));
    }
  }

  private PluginDto createDtoForInstalled(InstalledPlugin plugin, List<AvailablePlugin> availablePlugins) {
    PluginInformation information = plugin.getDescriptor().getInformation();
    Optional<AvailablePlugin> availablePlugin = checkForUpdates(plugin, availablePlugins);

    Links.Builder links = linkingTo()
      .self(resourceLinks.installedPlugin()
        .self(information.getName()));
    if (!plugin.isCore()
      && availablePlugin.isPresent()
      && !availablePlugin.get().isPending()
      && PluginPermissions.write().isPermitted()
    ) {
      String href = resourceLinks.availablePlugin().install(information.getName());
      appendLink(links, "update", href);
    }

    if (plugin.isUninstallable()
      && (!availablePlugin.isPresent() || !availablePlugin.get().isPending())
      && PluginPermissions.write().isPermitted()
    ) {
      String href = resourceLinks.installedPlugin().uninstall(information.getName());
      appendLink(links, "uninstall", href);
    }

    PluginDto dto = new PluginDto(links.build());

    availablePlugin.ifPresent(value -> {
      dto.setNewVersion(value.getDescriptor().getInformation().getVersion());
      dto.setPending(value.isPending());
    });

    dto.setCore(plugin.isCore());
    dto.setMarkedForUninstall(plugin.isMarkedForUninstall());

    return dto;
  }

  private Optional<AvailablePlugin> checkForUpdates(InstalledPlugin plugin, List<AvailablePlugin> availablePlugins) {
    return availablePlugins.stream()
      .filter(a -> a.getDescriptor().getInformation().getName().equals(plugin.getDescriptor().getInformation().getName()))
      .findAny();
  }
}
