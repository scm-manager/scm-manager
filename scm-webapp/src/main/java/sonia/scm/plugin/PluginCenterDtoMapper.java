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

package sonia.scm.plugin;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Map;
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
      // plugin center api returns always a download link,
      // but for cloudogu plugin without authentication the href is an empty string
      String url = plugin.getLinks().get("download").getHref();
      String installLink = getInstallLink(plugin);
      AvailablePluginDescriptor descriptor = new AvailablePluginDescriptor(
        map(plugin), map(plugin.getConditions()), plugin.getDependencies(), plugin.getOptionalDependencies(), url, plugin.getSha256sum(), installLink
      );
      plugins.add(new AvailablePlugin(descriptor));
    }
    return new PluginCenterResult(plugins, pluginSets);
  }

  private String getInstallLink(PluginCenterDto.Plugin plugin) {
    PluginCenterDto.Link link = plugin.getLinks().get("install");
    if (link != null) {
      return link.getHref();
    }
    return null;
  }
}
