package sonia.scm.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

class PluginDependencyTracker {

  private final Map<String, Collection<String>> plugins = new HashMap<>();

  void addInstalled(PluginDescriptor plugin) {
    if (plugin.getDependencies() != null) {
      plugin.getDependencies().forEach(dependency -> addDependency(plugin.getInformation().getName(), dependency));
    }
  }

  void removeInstalled(PluginDescriptor plugin) {
    doThrow()
      .violation("Plugin is needed as a dependency for other plugins", "plugin")
      .when(!mayUninstall(plugin.getInformation().getName()));
    plugin.getDependencies().forEach(dependency -> removeDependency(plugin.getInformation().getName(), dependency));
  }

  boolean mayUninstall(String name) {
    return plugins.computeIfAbsent(name, x -> new HashSet<>()).isEmpty();
  }

  private void addDependency(String from, String to) {
    plugins.computeIfAbsent(to, name -> new HashSet<>()).add(from);
  }

  private void removeDependency(String from, String to) {
    Collection<String> dependencies = plugins.get(to);
    if (dependencies == null) {
      throw new NullPointerException("inverse dependencies not found for " + to);
    }
    dependencies.remove(from);
  }
}
