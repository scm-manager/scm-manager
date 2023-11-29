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
