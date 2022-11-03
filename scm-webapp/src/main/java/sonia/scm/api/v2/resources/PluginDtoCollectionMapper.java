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

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import sonia.scm.plugin.AvailablePlugin;
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
    return new PluginCollectionDto(createInstalledPluginsLinks(), embedDtos(dtos), plugins.isPluginCenterError());
  }

  public PluginCollectionDto mapAvailable(List<AvailablePlugin> plugins, boolean isPluginCenterError) {
    List<PluginDto> dtos = plugins.stream().map(mapper::mapAvailable).collect(toList());
    return new PluginCollectionDto(createAvailablePluginsLinks(plugins), embedDtos(dtos), isPluginCenterError);
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
