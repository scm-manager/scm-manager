package sonia.scm.plugin;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginCenterDto.Plugin;
import static sonia.scm.plugin.PluginCenterDto.*;

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
      "SCM Hitchhiker Plugin",
      "plugin for hitchhikers",
      "Travel",
      "2.0.0",
      "trillian",
      "http://avatar.url",
      "555000444",
      new Condition(Collections.singletonList("linux"), "amd64","2.0.0"),
      ImmutableSet.of("scm-review-plugin"),
      new HashMap<>());

    when(dto.getEmbedded().getPlugins()).thenReturn(Collections.singletonList(plugin));
    AvailablePluginDescriptor descriptor = mapper.map(dto).iterator().next().getDescriptor();
    PluginInformation information = descriptor.getInformation();
    PluginCondition condition = descriptor.getCondition();

    assertThat(information.getAuthor()).isEqualTo(plugin.getAuthor());
    assertThat(information.getCategory()).isEqualTo(plugin.getCategory());
    assertThat(information.getVersion()).isEqualTo(plugin.getVersion());
    assertThat(condition.getArch()).isEqualTo(plugin.getConditions().getArch());
    assertThat(condition.getMinVersion()).isEqualTo(plugin.getConditions().getMinVersion());
    assertThat(condition.getOs().iterator().next()).isEqualTo(plugin.getConditions().getOs().iterator().next());
    assertThat(information.getDescription()).isEqualTo(plugin.getDescription());
    assertThat(information.getName()).isEqualTo(plugin.getName());
  }

  @Test
  void shouldMapMultiplePlugins() {
    Plugin plugin1 = new Plugin(
      "scm-review-plugin",
      "SCM Hitchhiker Plugin",
      "plugin for hitchhikers",
      "Travel",
      "2.1.0",
      "trillian",
      "https://avatar.url",
      "12345678aa",
      new Condition(Collections.singletonList("linux"), "amd64","2.0.0"),
      ImmutableSet.of("scm-review-plugin"),
      new HashMap<>());

    Plugin plugin2 = new Plugin(
      "scm-hitchhiker-plugin",
      "SCM Hitchhiker Plugin",
      "plugin for hitchhikers",
      "Travel",
      "2.0.0",
      "dent",
      "http://avatar.url",
      "555000444",
      new Condition(Collections.singletonList("linux"), "amd64","2.0.0"),
      ImmutableSet.of("scm-review-plugin"),
      new HashMap<>());

    when(dto.getEmbedded().getPlugins()).thenReturn(Arrays.asList(plugin1, plugin2));

    Set<AvailablePlugin> resultSet = mapper.map(dto);

    List<AvailablePlugin> pluginsList = new ArrayList<>(resultSet);

    PluginInformation pluginInformation1 = pluginsList.get(1).getDescriptor().getInformation();
    PluginInformation pluginInformation2 = pluginsList.get(0).getDescriptor().getInformation();

    assertThat(pluginInformation1.getAuthor()).isEqualTo(plugin1.getAuthor());
    assertThat(pluginInformation1.getVersion()).isEqualTo(plugin1.getVersion());
    assertThat(pluginInformation2.getAuthor()).isEqualTo(plugin2.getAuthor());
    assertThat(pluginInformation2.getVersion()).isEqualTo(plugin2.getVersion());
    assertThat(resultSet.size()).isEqualTo(2);
  }
}
