package sonia.scm.plugin;

import java.util.Optional;
import java.util.Set;

/**
 * @since 2.0.0
 */
public class AvailablePluginDescriptor implements PluginDescriptor {

  private final PluginInformation information;
  private final PluginCondition condition;
  private final Set<String> dependencies;
  private final String url;
  private final String checksum;

  public AvailablePluginDescriptor(PluginInformation information, PluginCondition condition, Set<String> dependencies, String url, String checksum) {
    this.information = information;
    this.condition = condition;
    this.dependencies = dependencies;
    this.url = url;
    this.checksum = checksum;
  }

  public String getUrl() {
    return url;
  }

  public Optional<String> getChecksum() {
    return Optional.ofNullable(checksum);
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
