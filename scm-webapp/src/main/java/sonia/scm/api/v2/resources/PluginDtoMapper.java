/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginPermissions;

import javax.inject.Inject;

import java.util.List;
import java.util.Optional;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class PluginDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

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
    return dto;
  }

  private void map(PluginDto dto, Plugin plugin) {
    dto.setDependencies(plugin.getDescriptor().getDependencies());
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

    if (!plugin.isPending() && PluginPermissions.manage().isPermitted()) {
      links.single(link("install", resourceLinks.availablePlugin().install(information.getName())));
    }

    return new PluginDto(links.build());
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
      && PluginPermissions.manage().isPermitted()
    ) {
      links.single(link("update", resourceLinks.availablePlugin().install(information.getName())));
    }
    if (plugin.isUninstallable()
      && (!availablePlugin.isPresent() || !availablePlugin.get().isPending())
      && PluginPermissions.manage().isPermitted()
    ) {
      links.single(link("uninstall", resourceLinks.installedPlugin().uninstall(information.getName())));
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
