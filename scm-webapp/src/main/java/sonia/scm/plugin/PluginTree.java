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
    PluginCondition condition = plugin.getCondition();
    if (!condition.isSupported()) {
      if (smp.isCore() && stage == Stage.DEVELOPMENT) {
        LOG.warn("plugin {} does not match conditions {}", plugin.getInformation().getId(), condition);
      } else {
        throw new PluginConditionFailedException(
          condition,
          String.format(
            "could not load plugin %s, the plugin condition does not match: %s",
            plugin.getInformation().getId(), condition
          )
        );
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
