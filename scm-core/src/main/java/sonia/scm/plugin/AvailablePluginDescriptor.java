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

import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * @since 2.0.0
 */
public class AvailablePluginDescriptor implements PluginDescriptor {

  private final PluginInformation information;
  private final PluginCondition condition;
  private final Set<String> dependencies;
  private final Set<String> optionalDependencies;
  private final String url;
  private final String checksum;

  /**
   * @deprecated Use {@link #AvailablePluginDescriptor(PluginInformation, PluginCondition, Set, Set, String, String)} instead
   */
  @Deprecated
  public AvailablePluginDescriptor(PluginInformation information, PluginCondition condition, Set<String> dependencies, String url, String checksum) {
    this(information, condition, dependencies, emptySet(), url, checksum);
  }

  public AvailablePluginDescriptor(PluginInformation information, PluginCondition condition, Set<String> dependencies, Set<String> optionalDependencies, String url, String checksum) {
    this.information = information;
    this.condition = condition;
    this.dependencies = dependencies;
    this.optionalDependencies = optionalDependencies;
    this.url = url;
    this.checksum = checksum;
  }

  public String getUrl() {
    return url;
  }

  public Optional<String> getChecksum() {
    return Optional.ofNullable(checksum);
  }

  @Override
  public PluginInformation getInformation() {
    return information;
  }

  @Override
  public PluginCondition getCondition() {
    return condition;
  }

  @Override
  public Set<String> getDependencies() {
    return dependencies;
  }

  @Override
  public Set<String> getOptionalDependencies() {
    return optionalDependencies;
  }
}
