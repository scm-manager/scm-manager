package sonia.scm.plugin;

public interface Plugin {

  PluginDescriptor getDescriptor();

  /**
   * Returns plugin state.
   *
   * @deprecated State is now derived from concrete plugin implementations
   * @return plugin state
   */
  @Deprecated
  PluginState getState();

}
