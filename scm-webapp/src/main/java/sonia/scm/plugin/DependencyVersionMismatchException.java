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

import lombok.Getter;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Getter
@SuppressWarnings("java:S110")
public class DependencyVersionMismatchException extends PluginInstallException {

  private final String plugin;
  private final String dependency;
  private final String minVersion;
  private final String currentVersion;

  public DependencyVersionMismatchException(String plugin, String dependency, String minVersion, String currentVersion) {
    super(
      entity("Dependency", dependency)
        .in("Plugin", plugin)
        .build(),
      String.format(
        "%s requires dependency %s at least in version %s, but it is installed in version %s",
        plugin, dependency, minVersion, currentVersion
      )
    );
    this.plugin = plugin;
    this.dependency = dependency;
    this.minVersion = minVersion;
    this.currentVersion = currentVersion;
  }

  @Override
  public String getCode() {
    return "E5S6niWwi1";
  }
}
