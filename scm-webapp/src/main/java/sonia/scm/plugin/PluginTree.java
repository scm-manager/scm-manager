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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Stage;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;


public final class PluginTree {

  private static final int SCM_VERSION = 3;


  private static final Logger LOG = LoggerFactory.getLogger(PluginTree.class);

  private final Stage stage;
  private final List<PluginNode> rootNodes;

  public PluginTree(Stage stage, ExplodedSmp... smps) {
    this(stage, Arrays.asList(smps));
  }

  public PluginTree(Stage stage, Collection<ExplodedSmp> smps) {
    this.stage = stage;
    smps.forEach(s -> {
      InstalledPluginDescriptor plugin = s.getPlugin();
      LOG.trace("plugin: {}", plugin.getInformation().getName());
      LOG.trace("dependencies: {}", plugin.getDependencies());
      LOG.trace("optional dependencies: {}", plugin.getOptionalDependencies());
    });

    rootNodes = new SmpNodeBuilder().buildNodeTree(smps);

    for (ExplodedSmp smp : smps) {
      checkIfSupported(smp);
    }
  }

  private void checkIfSupported(ExplodedSmp smp) {
    InstalledPluginDescriptor plugin = smp.getPlugin();
    checkIfConditionsMatch(smp, plugin);
  }

  private void checkIfConditionsMatch(ExplodedSmp smp, InstalledPluginDescriptor plugin) {
    PluginCondition pluginCondition = plugin.getCondition();
    if (!pluginCondition.getConditionCheckResult().equals(PluginCondition.CheckResult.OK)) {
      if (smp.isCore() && stage == Stage.DEVELOPMENT) {
        LOG.warn("plugin {} does not match conditions {}", plugin.getInformation().getId(), pluginCondition);
      } else {
        throw new PluginConditionFailedException(plugin.getInformation().getId(), pluginCondition);
      }
    }
  }

  public List<PluginNode> getLeafLastNodes() {
    LinkedHashSet<PluginNode> leafFirst = new LinkedHashSet<>();

    rootNodes.forEach(node -> appendLeafFirst(leafFirst, node));

    LinkedList<PluginNode> leafLast = new LinkedList<>();
    leafFirst.forEach(leafLast::addFirst);
    return leafLast;
  }

  private void appendLeafFirst(LinkedHashSet<PluginNode> leafFirst, PluginNode node) {
    node.getChildren().forEach(child -> appendLeafFirst(leafFirst, child));
    leafFirst.add(node);
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    for (PluginNode node : rootNodes) {
      append(buffer, "", node);
    }
    return buffer.toString();
  }

  private void append(StringBuilder buffer, String indent, PluginNode node) {
    PluginInformation information = node.getPlugin().getPlugin().getInformation();
    buffer.append(indent).append("+- ").append(node.getId()).append("@").append(information.getVersion()).append("\n");
    for (PluginNode child : node.getChildren()) {
      append(buffer, indent + "   ", child);
    }
  }

}
