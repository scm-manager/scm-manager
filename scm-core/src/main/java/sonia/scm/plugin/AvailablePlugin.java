package sonia.scm.plugin;

import com.google.common.base.Preconditions;

public class AvailablePlugin implements Plugin {

  private final AvailablePluginDescriptor pluginDescriptor;
  private final boolean pending;

  public AvailablePlugin(AvailablePluginDescriptor pluginDescriptor) {
    this(pluginDescriptor, false);
  }

  private AvailablePlugin(AvailablePluginDescriptor pluginDescriptor, boolean pending) {
    this.pluginDescriptor = pluginDescriptor;
    this.pending = pending;
  }

  @Override
  public AvailablePluginDescriptor getDescriptor() {
    return pluginDescriptor;
  }

  @Override
  public PluginState getState() {
    return PluginState.AVAILABLE;
  }

  public boolean isPending() {
    return pending;
  }

  public AvailablePlugin install() {
    Preconditions.checkState(!pending, "installation is already pending");
    return new AvailablePlugin(pluginDescriptor, true);
  }
}
