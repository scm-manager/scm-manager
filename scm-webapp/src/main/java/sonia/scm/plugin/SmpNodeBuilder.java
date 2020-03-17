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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class SmpNodeBuilder {

  List<PluginNode> buildNodeTree(ExplodedSmp[] smps) {
    return buildNodeTree(Arrays.asList(smps));
  }

  List<PluginNode> buildNodeTree(Collection<ExplodedSmp> smps) {
    Set<String> availablePlugins = getAvailablePluginNames(smps);
    smps.forEach(smp -> this.assertDependenciesFulfilled(availablePlugins, smp));

    List<PluginNode> nodes = createNodes(smps);

    nodes.forEach(node ->
      nodes.forEach(otherNode -> appendIfDependsOnOtherNode(node, otherNode))
    );

    nodes.forEach(this::checkForCircle);

    return nodes;
  }

  private Set<String> getAvailablePluginNames(Collection<ExplodedSmp> smps) {
    return smps.stream()
      .map(ExplodedSmp::getPlugin)
      .map(InstalledPluginDescriptor::getInformation)
      .map(PluginInformation::getName)
      .collect(Collectors.toSet());
  }

  private void appendIfDependsOnOtherNode(PluginNode node, PluginNode otherNode) {
    if (node.getPlugin().getPlugin().getDependenciesInclusiveOptionals().contains(otherNode.getId())
      && !otherNode.getChildren().contains(node)) {
      otherNode.addChild(node);
    }
  }

  private List<PluginNode> createNodes(Collection<ExplodedSmp> smps) {
    return smps.stream().map(PluginNode::new).collect(Collectors.toList());
  }

  private void assertDependenciesFulfilled(Set<String> availablePlugins, ExplodedSmp smp) {
    smp.getPlugin().getDependencies().forEach(dependency -> {
      if (!availablePlugins.contains(dependency)) {
        throw new PluginNotInstalledException(
          String.format(
            "dependency %s of %s is not installed",
            dependency,
            smp.getPlugin().getInformation().getName()
          )
        );
      }
    });
  }

  private void checkForCircle(PluginNode pluginNode) {
    pluginNode.getChildren().forEach(child -> assertDoesNotContainsDependency(pluginNode, child));
  }

  private void assertDoesNotContainsDependency(PluginNode pluginNode, PluginNode childNode) {
    if (childNode == pluginNode) {
      throw new PluginCircularDependencyException("circular dependency detected: " +
        childNode.getId() + " depends on " + pluginNode.getId() + " and " +
        pluginNode.getId() + " depends on " + childNode.getId()
      );
    }
    childNode.getChildren().forEach(child -> assertDoesNotContainsDependency(pluginNode, child));
  }
}
