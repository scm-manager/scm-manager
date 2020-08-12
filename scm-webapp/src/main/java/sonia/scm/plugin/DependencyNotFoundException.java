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

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class DependencyNotFoundException extends PluginInstallException {

  private final String plugin;
  private final String missingDependency;

  public DependencyNotFoundException(String plugin, String missingDependency) {
    super(
      entity("Dependency", missingDependency)
        .in("Plugin", plugin)
        .build(),
      String.format(
        "missing dependency %s of plugin %s",
        missingDependency,
        plugin
      )
    );
    this.plugin = plugin;
    this.missingDependency = missingDependency;
  }

  public String getPlugin() {
    return plugin;
  }

  public String getMissingDependency() {
    return missingDependency;
  }

  @Override
  public String getCode() {
    return "5GS6lwvWF1";
  }
}
