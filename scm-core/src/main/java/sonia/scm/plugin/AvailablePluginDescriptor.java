package sonia.scm.plugin;

import java.util.Set;

/**
 * @since 2.0.0
 */
public class AvailablePluginDescriptor implements PluginDescriptor {

  private final PluginInformation information;
  private final PluginCondition condition;
  private final Set<String> dependencies;

  public AvailablePluginDescriptor(PluginInformation information, PluginCondition condition, Set<String> dependencies) {
    this.information = information;
    this.condition = condition;
    this.dependencies = dependencies;
  }

  @Override
  public PluginInformation getInformation() {
    return information;
  }

  @Override
  public PluginCondition getCondition() {
    return condition;
  }

  @Override
  public Set<String> getDependencies() {
    return dependencies;
  }
}
