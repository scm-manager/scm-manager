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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.PluginSet;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginTestHelper.createAvailable;

@ExtendWith(MockitoExtension.class)
class PluginSetDtoMapperTest {

  @Mock
  private PluginDtoMapper pluginDtoMapper;

  @InjectMocks
  private PluginSetDtoMapper mapper;

  @Test
  void shouldMap() {
    List<AvailablePlugin> availablePlugins = createAvailablePlugins();
    ImmutableSet<PluginSet> pluginSets = createPluginSets();

    List<PluginSetDto> dtos = mapper.map(pluginSets, availablePlugins, Locale.ENGLISH);

    assertThat(dtos).hasSize(2);
    PluginSetDto first = dtos.get(0);
    assertThat(first.getSequence()).isZero();
    assertThat(first.getName()).isEqualTo("My Plugin Set 2");
    assertThat(first.getFeatures()).contains("this is also awesome!");
    assertThat(first.getImages()).isNotEmpty();
    assertThat(first.getPlugins()).hasSize(2);

    assertThat(dtos.get(1).getSequence()).isEqualTo(1);
  }

  @Test
  void shouldMapWithOtherLanguage() {
    List<AvailablePlugin> availablePlugins = createAvailablePlugins();
    ImmutableSet<PluginSet> pluginSets = createPluginSets();

    List<PluginSetDto> dtos = mapper.map(pluginSets, availablePlugins, Locale.FRENCH);

    assertThat(dtos).hasSize(2);
    PluginSetDto first = dtos.get(0);
    assertThat(first.getName()).isEqualTo("My Plugin Set 2");
  }

  private List<AvailablePlugin> createAvailablePlugins() {
    AvailablePlugin git = createAvailable("scm-git-plugin");
    AvailablePlugin svn = createAvailable("scm-svn-plugin");
    AvailablePlugin hg = createAvailable("scm-hg-plugin");
    PluginDto gitDto = new PluginDto();
    gitDto.setName("scm-git-plugin");
    PluginDto svnDto = new PluginDto();
    svnDto.setName("scm-svn-plugin");
    PluginDto hgDto = new PluginDto();
    hgDto.setName("scm-hg-plugin");

    when(pluginDtoMapper.mapAvailable(git)).thenReturn(gitDto);
    when(pluginDtoMapper.mapAvailable(svn)).thenReturn(svnDto);
    when(pluginDtoMapper.mapAvailable(hg)).thenReturn(hgDto);

    return List.of(git, svn, hg);
  }

  private ImmutableSet<PluginSet> createPluginSets() {
    PluginSet pluginSet = new PluginSet(
      "my-plugin-set",
      1,
      ImmutableSet.of("scm-git-plugin", "scm-hg-plugin"),
      ImmutableMap.of("en", new PluginSet.Description("My Plugin Set", List.of("this is awesome!"))),
      ImmutableMap.of("standard", "base64image")
    );

    PluginSet pluginSet2 = new PluginSet(
      "my-other-plugin-set",
      0,
      ImmutableSet.of("scm-svn-plugin", "scm-hg-plugin"),
      ImmutableMap.of("en", new PluginSet.Description("My Plugin Set 2", List.of("this is also awesome!"))),
      ImmutableMap.of("standard", "base64image")
    );
    return ImmutableSet.of(pluginSet, pluginSet2);
  }
}
