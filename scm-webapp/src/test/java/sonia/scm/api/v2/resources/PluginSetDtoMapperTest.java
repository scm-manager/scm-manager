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

    List<AvailablePlugin> availablePlugins = List.of(git, svn, hg);

    PluginSet pluginSet = new PluginSet(
      "my-plugin-set",
      1,
      ImmutableSet.of("scm-git-plugin", "scm-hg-plugin"),
      ImmutableMap.of("en", new PluginSet.Description("My Plugin Set", ImmutableSet.of("this is awesome!"))),
      ImmutableMap.of("standard", "base64image")
    );

    PluginSet pluginSet2 = new PluginSet(
      "my-other-plugin-set",
      0,
      ImmutableSet.of("scm-svn-plugin", "scm-hg-plugin"),
      ImmutableMap.of("en", new PluginSet.Description("My Plugin Set 2", ImmutableSet.of("this is also awesome!"))),
      ImmutableMap.of("standard", "base64image")
    );
    ImmutableSet<PluginSet> pluginSets = ImmutableSet.of(pluginSet, pluginSet2);

    List<PluginSetDto> dtos = mapper.map(pluginSets, availablePlugins, Locale.ENGLISH);
    assertThat(dtos).hasSize(2);
    PluginSetDto first = dtos.get(0);
    assertThat(first.getSequence()).isEqualTo(0);
    assertThat(first.getName()).isEqualTo("My Plugin Set 2");
    assertThat(first.getFeatures()).contains("this is also awesome!");
    assertThat(first.getImages()).isNotEmpty();
    assertThat(first.getPlugins()).hasSize(2);

    assertThat(dtos.get(1).getSequence()).isEqualTo(1);
  }
}
