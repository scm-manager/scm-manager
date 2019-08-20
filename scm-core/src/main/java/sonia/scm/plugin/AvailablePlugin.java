package sonia.scm.plugin;

public class AvailablePlugin implements Plugin {

  private final AvailablePluginDescriptor pluginDescriptor;

  public AvailablePlugin(AvailablePluginDescriptor pluginDescriptor) {
    this.pluginDescriptor = pluginDescriptor;
  }

  @Override
  public AvailablePluginDescriptor getDescriptor() {
    return pluginDescriptor;
  }

  @Override
  public PluginState getState() {
    return PluginState.AVAILABLE;
  }
}
