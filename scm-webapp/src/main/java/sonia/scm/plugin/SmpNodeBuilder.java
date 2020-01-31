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

    Set<String> availablePlugins = smps.stream().map(smp -> smp.getPlugin().getInformation().getName()).collect(Collectors.toSet());

    smps.forEach(smp -> this.assertDependenciesFulfilled(availablePlugins, smp));

    List<PluginNode> nodes = smps.stream().map(PluginNode::new).collect(Collectors.toList());

    nodes.forEach(node -> {
      ExplodedSmp smp = node.getPlugin();
      nodes.forEach(otherNode -> {

        if (smp.getPlugin().getDependenciesInclusiveOptionals().contains(otherNode.getId())
          && !otherNode.getChildren().contains(node)) {
          otherNode.addChild(node);
        }
      });

      nodes.forEach(this::checkForCircle);
    });

    return nodes;
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
