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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginCenterDto.Condition;
import static sonia.scm.plugin.PluginCenterDto.Link;
import static sonia.scm.plugin.PluginCenterDto.Plugin;

@ExtendWith(MockitoExtension.class)
class PluginCenterDtoMapperTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PluginCenterDto dto;

  @InjectMocks
  private PluginCenterDtoMapperImpl mapper;

  @Test
  void shouldMapSinglePlugin() {
    Plugin plugin = new Plugin(
      "scm-hitchhiker-plugin",
      "2.0.0",
      "SCM Hitchhiker Plugin",
      "plugin for hitchhikers",
      "Travel",
      "trillian",
      "http://avatar.url",
      "555000444",
      PluginInformation.PluginType.SCM,
      new Condition(Collections.singletonList("linux"), "amd64","2.0.0"),
      ImmutableSet.of("scm-review-plugin"),
      ImmutableSet.of(),
      ImmutableMap.of("download", new Link("http://download.hitchhiker.com"))
    );

    PluginCenterDto.PluginSet pluginSet = new PluginCenterDto.PluginSet(
      "my-plugin-set",
      ">2.0.0",
      0,
      ImmutableSet.of("scm-review-plugin"),
      ImmutableMap.of("en", new PluginCenterDto.Description("My Plugin Set", List.of("hello world"))),
      ImmutableMap.of("standard", "base64image")
    );

    when(dto.getEmbedded().getPlugins()).thenReturn(Collections.singletonList(plugin));
    when(dto.getEmbedded().getPluginSets()).thenReturn(Collections.singletonList(pluginSet));
    PluginCenterResult mapped = mapper.map(dto);
    AvailablePluginDescriptor descriptor = mapped.getPlugins().iterator().next().getDescriptor();
    PluginInformation information = descriptor.getInformation();
    PluginCondition condition = descriptor.getCondition();

    assertThat(descriptor.getUrl()).isEqualTo("http://download.hitchhiker.com");
    assertThat(descriptor.getChecksum()).contains("555000444");

    assertThat(information.getAuthor()).isEqualTo(plugin.getAuthor());
    assertThat(information.getCategory()).isEqualTo(plugin.getCategory());
    assertThat(information.getVersion()).isEqualTo(plugin.getVersion());
    assertThat(condition.getArch()).isEqualTo(plugin.getConditions().getArch());
    assertThat(condition.getMinVersion()).isEqualTo(plugin.getConditions().getMinVersion());
    assertThat(condition.getOs().iterator().next()).isEqualTo(plugin.getConditions().getOs().iterator().next());
    assertThat(information.getDescription()).isEqualTo(plugin.getDescription());
    assertThat(information.getName()).isEqualTo(plugin.getName());

    PluginSet mappedPluginSet = mapped.getPluginSets().iterator().next();

    assertThat(mappedPluginSet.getId()).isEqualTo(pluginSet.getId());
    assertThat(mappedPluginSet.getSequence()).isEqualTo(pluginSet.getSequence());
    assertThat(mappedPluginSet.getPlugins()).hasSize(pluginSet.getPlugins().size());
    assertThat(mappedPluginSet.getImages()).isNotEmpty();
    assertThat(mappedPluginSet.getDescriptions()).isNotEmpty();
  }

  @Test
  void shouldMapMultiplePlugins() {
    Plugin plugin1 = new Plugin(
      "scm-review-plugin",
      "2.1.0",
      "SCM Hitchhiker Plugin",
      "plugin for hitchhikers",
      "Travel",
      "trillian",
      "https://avatar.url",
      "12345678aa",
      PluginInformation.PluginType.SCM,
      new Condition(Collections.singletonList("linux"), "amd64","2.0.0"),
      ImmutableSet.of("scm-review-plugin"),
      ImmutableSet.of(),
      ImmutableMap.of("download", new Link("http://download.hitchhiker.com/review"))
    );

    Plugin plugin2 = new Plugin(
      "scm-hitchhiker-plugin",
      "2.0.0",
      "SCM Hitchhiker Plugin",
      "plugin for hitchhikers",
      "Travel",
      "dent",
      "http://avatar.url",
      "555000444",
      PluginInformation.PluginType.CLOUDOGU,
      new Condition(Collections.singletonList("linux"), "amd64","2.0.0"),
      ImmutableSet.of("scm-review-plugin"),
      ImmutableSet.of(),
      ImmutableMap.of("download", new Link("http://download.hitchhiker.com/hitchhiker"))
    );

    when(dto.getEmbedded().getPlugins()).thenReturn(Arrays.asList(plugin1, plugin2));

    PluginCenterResult pluginCenterResult = mapper.map(dto);
    Set<AvailablePlugin> resultSet = pluginCenterResult.getPlugins();

    PluginInformation pluginInformation1 = findPlugin(resultSet, plugin1.getName());
    PluginInformation pluginInformation2 = findPlugin(resultSet, plugin2.getName());

    assertThat(pluginInformation1.getAuthor()).isEqualTo(plugin1.getAuthor());
    assertThat(pluginInformation1.getVersion()).isEqualTo(plugin1.getVersion());
    assertThat(pluginInformation2.getAuthor()).isEqualTo(plugin2.getAuthor());
    assertThat(pluginInformation2.getVersion()).isEqualTo(plugin2.getVersion());
    assertThat(pluginInformation1.getType()).isEqualTo(PluginInformation.PluginType.SCM);
    assertThat(pluginInformation2.getType()).isEqualTo(PluginInformation.PluginType.CLOUDOGU);
    assertThat(resultSet.size()).isEqualTo(2);
  }

  private PluginInformation findPlugin(Set<AvailablePlugin> resultSet, String name) {
    return resultSet
      .stream()
      .filter(p -> name.equals(p.getDescriptor().getInformation().getName()))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("could not find plugin " + name))
      .getDescriptor()
      .getInformation();
  }
}
