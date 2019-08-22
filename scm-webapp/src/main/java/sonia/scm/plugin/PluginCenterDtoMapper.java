package sonia.scm.plugin;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Set;

@Mapper
public abstract class PluginCenterDtoMapper  {

  static final PluginCenterDtoMapper INSTANCE = Mappers.getMapper(PluginCenterDtoMapper.class);

  abstract PluginInformation map(PluginCenterDto.Plugin plugin);
  abstract PluginCondition map(PluginCenterDto.Condition condition);

  Set<AvailablePlugin> map(PluginCenterDto pluginCenterDto) {
    Set<AvailablePlugin> plugins = new HashSet<>();
    for (PluginCenterDto.Plugin plugin : pluginCenterDto.getEmbedded().getPlugins()) {
      String url = plugin.getLinks().get("download").getHref();
      AvailablePluginDescriptor descriptor = new AvailablePluginDescriptor(
        map(plugin), map(plugin.getConditions()), plugin.getDependencies(), url, plugin.getSha256()
      );
      plugins.add(new AvailablePlugin(descriptor));
    }
    return plugins;
  }
}
