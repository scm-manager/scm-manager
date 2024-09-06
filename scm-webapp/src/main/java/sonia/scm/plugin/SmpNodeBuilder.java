/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
