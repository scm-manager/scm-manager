package sonia.scm.plugin;

import com.google.common.base.Preconditions;

public class AvailablePlugin implements Plugin {

  private final AvailablePluginDescriptor pluginDescriptor;
  private boolean pending;

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

  public boolean isPending() {
    return pending;
  }

  public void cancelInstallation() {
    this.pending = false;
  }

  public AvailablePlugin install() {
    Preconditions.checkState(!pending, "installation is already pending");
    return new AvailablePlugin(pluginDescriptor, true);
  }
}
