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

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public final class PluginTree
{

  /** Field description */
  private static final int SCM_VERSION = 2;

  /**
   * the logger for PluginTree
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PluginTree.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param smps
   */
  public PluginTree(ExplodedSmp... smps)
  {
    this(Arrays.asList(smps));
  }

  /**
   * Constructs ...
   *
   *
   * @param smps
   */
  public PluginTree(Collection<ExplodedSmp> smps)
  {

    smps.forEach(s -> {
      InstalledPluginDescriptor plugin = s.getPlugin();
      logger.trace("plugin: {}", plugin.getInformation().getName());
      logger.trace("dependencies: {}", plugin.getDependencies());
      logger.trace("optional dependencies: {}", plugin.getOptionalDependencies());
    });

    rootNodes = new SmpNodeBuilder().buildNodeTree(smps);

    for (ExplodedSmp smp : smps)
    {
      InstalledPluginDescriptor plugin = smp.getPlugin();

      if (plugin.getScmVersion() != SCM_VERSION)
      {
        //J-
        throw new PluginException(
          String.format(
            "scm version %s of plugin %s does not match, required is version %s",
            plugin.getScmVersion(), plugin.getInformation().getId(), SCM_VERSION
          )
        );
        //J+
      }

      PluginCondition condition = plugin.getCondition();

      if (!condition.isSupported())
      {
        //J-
        throw new PluginConditionFailedException(
          condition,
          String.format(
            "could not load plugin %s, the plugin condition does not match: %s",
            plugin.getInformation().getId(), condition
          )
        );
        //J+
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<PluginNode> getLeafLastNodes()
  {
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


  //~--- methods --------------------------------------------------------------

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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final List<PluginNode> rootNodes;
}
