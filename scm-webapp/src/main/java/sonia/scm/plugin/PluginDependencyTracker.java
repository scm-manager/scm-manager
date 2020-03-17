/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
