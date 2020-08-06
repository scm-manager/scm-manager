/*
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PluginInstallationContext {

  private final Map<String, NameAndVersion> dependencies;

  private PluginInstallationContext(Map<String, NameAndVersion> dependencies) {
    this.dependencies = dependencies;
  }

  public static PluginInstallationContext empty() {
    return new PluginInstallationContext(Collections.emptyMap());
  }

  public static PluginInstallationContext fromDescriptors(Iterable<? extends PluginDescriptor> installed, Iterable<? extends PluginDescriptor> pending) {
    Map<String, NameAndVersion> dependencies = new HashMap<>();
    appendDescriptors(dependencies, installed);
    appendDescriptors(dependencies, pending);
    return new PluginInstallationContext(dependencies);
  }

  public static PluginInstallationContext from(Iterable<? extends Plugin> installed, Iterable<? extends Plugin> pending) {
    Map<String, NameAndVersion> dependencies = new HashMap<>();
    appendPlugins(dependencies, installed);
    appendPlugins(dependencies, pending);
    return new PluginInstallationContext(dependencies);
  }

  private static <P extends PluginDescriptor> void appendDescriptors(Map<String, NameAndVersion> dependencies, Iterable<P> descriptors) {
    descriptors.forEach(desc -> appendPlugins(dependencies, desc.getInformation()));
  }

  private static <P extends Plugin> void appendPlugins(Map<String, NameAndVersion> dependencies, Iterable<P> plugins) {
    plugins.forEach(plugin -> appendPlugins(dependencies, plugin.getDescriptor().getInformation()));
  }

  private static void appendPlugins(Map<String, NameAndVersion> dependencies, PluginInformation information) {
    dependencies.put(information.getName(), new NameAndVersion(information.getName(), information.getVersion()));
  }

  public Optional<NameAndVersion> find(String name) {
    return Optional.ofNullable(dependencies.get(name));
  }
}
