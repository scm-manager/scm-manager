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

import java.util.List;

import static com.google.common.collect.Iterables.contains;
import static java.util.stream.Collectors.toList;

public class PendingPlugins {

  private final List<AvailablePlugin> install;
  private final List<InstalledPlugin> update;
  private final List<InstalledPlugin> uninstall;

  public PendingPlugins(List<AvailablePlugin> availablePlugins, List<InstalledPlugin> installedPlugins) {
    List<AvailablePlugin> pending = availablePlugins
      .stream()
      .filter(AvailablePlugin::isPending)
      .collect(toList());

    this.install = pending
      .stream()
      .filter(a -> !contains(installedPlugins, a)).collect(toList());
    this.update = installedPlugins
      .stream()
      .filter(i -> contains(pending, i)).collect(toList());
    this.uninstall = installedPlugins
      .stream()
      .filter(InstalledPlugin::isMarkedForUninstall).collect(toList());
  }

  public List<AvailablePlugin> getInstall() {
    return install;
  }

  public List<InstalledPlugin> getUpdate() {
    return update;
  }

  public List<InstalledPlugin> getUninstall() {
    return uninstall;
  }

  public boolean isPending(String name) {
    return uninstall.stream().anyMatch(p -> p.getDescriptor().getInformation().getName().equals(name))
      || update.stream().anyMatch(p -> p.getDescriptor().getInformation().getName().equals(name))
      || install.stream().anyMatch(p -> p.getDescriptor().getInformation().getName().equals(name));
  }

  public boolean existPendingChanges() {
    return !uninstall.isEmpty() || !update.isEmpty() || !install.isEmpty();
  }
}
