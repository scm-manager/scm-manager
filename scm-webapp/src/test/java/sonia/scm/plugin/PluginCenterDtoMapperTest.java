package sonia.scm.plugin;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.plugin.PluginCenterDto.Plugin;
import static sonia.scm.plugin.PluginCenterDto.*;

class PluginCenterDtoMapperTest {

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

    PluginInformation result = PluginCenterDtoMapper.map(Collections.singletonList(plugin)).iterator().next();

    assertThat(result.getAuthor()).isEqualTo(plugin.getAuthor());
    assertThat(result.getCategory()).isEqualTo(plugin.getCategory());
    assertThat(result.getVersion()).isEqualTo(plugin.getVersion());
    assertThat(result.getCondition().getArch()).isEqualTo(plugin.getConditions().getArch());
    assertThat(result.getCondition().getMinVersion()).isEqualTo(plugin.getConditions().getMinVersion());
    assertThat(result.getCondition().getOs().iterator().next()).isEqualTo(plugin.getConditions().getOs().iterator().next());
    assertThat(result.getDescription()).isEqualTo(plugin.getDescription());
    assertThat(result.getName()).isEqualTo(plugin.getName());
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

    Set<PluginInformation> resultSet = PluginCenterDtoMapper.map(Arrays.asList(plugin1, plugin2));

    List<PluginInformation> pluginsList = new ArrayList<>(resultSet);

    PluginInformation pluginInformation1 = pluginsList.get(1);
    PluginInformation pluginInformation2 = pluginsList.get(0);

    assertThat(pluginInformation1.getAuthor()).isEqualTo(plugin1.getAuthor());
    assertThat(pluginInformation1.getVersion()).isEqualTo(plugin1.getVersion());
    assertThat(pluginInformation2.getAuthor()).isEqualTo(plugin2.getAuthor());
    assertThat(pluginInformation2.getVersion()).isEqualTo(plugin2.getVersion());
    assertThat(resultSet.size()).isEqualTo(2);
  }
}
