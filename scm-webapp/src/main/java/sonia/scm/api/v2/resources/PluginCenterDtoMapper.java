package sonia.scm.api.v2.resources;

import sonia.scm.plugin.PluginCondition;
import sonia.scm.plugin.PluginInformation;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginCenterDtoMapper  {

  public static Set<PluginInformation> map(List<PluginCenterDto.Plugin> plugins) {
    HashSet<PluginInformation> pluginInformationSet = new HashSet<>();

    for (PluginCenterDto.Plugin plugin : plugins) {

      PluginInformation pluginInformation = new PluginInformation();
      pluginInformation.setName(plugin.getName());
      pluginInformation.setAuthor(plugin.getAuthor());
      pluginInformation.setCategory(plugin.getCategory());
      pluginInformation.setVersion(plugin.getVersion());
      pluginInformation.setDescription(plugin.getDescription());

      if (plugin.getConditions() != null) {
        PluginCenterDto.Condition condition = plugin.getConditions();
        pluginInformation.setCondition(new PluginCondition(condition.getMinVersion(), Collections.singletonList(condition.getOs()), condition.getArch()));
      }

      pluginInformationSet.add(pluginInformation);
    }
    return pluginInformationSet;
  }
}
