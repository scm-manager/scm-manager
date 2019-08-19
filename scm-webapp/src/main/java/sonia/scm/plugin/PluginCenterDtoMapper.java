package sonia.scm.plugin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper
public interface PluginCenterDtoMapper  {

  @Mapping(source = "conditions", target = "condition")
  PluginInformation map(PluginCenterDto.Plugin plugin);

  PluginCondition map(PluginCenterDto.Condition condition);

  static Set<PluginInformation> map(List<PluginCenterDto.Plugin> dtos) {
    PluginCenterDtoMapper mapper = Mappers.getMapper(PluginCenterDtoMapper.class);
    Set<PluginInformation> plugins = new HashSet<>();
    for (PluginCenterDto.Plugin plugin : dtos) {
      plugins.add(mapper.map(plugin));
    }
    return plugins;
  }
}
