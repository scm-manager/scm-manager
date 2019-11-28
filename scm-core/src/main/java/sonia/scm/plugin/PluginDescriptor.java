package sonia.scm.plugin;

import java.util.Set;

public interface PluginDescriptor {

  PluginInformation getInformation();

  PluginCondition getCondition();

  Set<String> getDependencies();

}
