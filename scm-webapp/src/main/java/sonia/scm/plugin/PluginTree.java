/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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
  public PluginTree(List<ExplodedSmp> smps)
  {
    Iterable<ExplodedSmp> smpOrdered = Ordering.natural().sortedCopy(smps);

    for (ExplodedSmp smp : smpOrdered)
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

      if ((condition == null) || condition.isSupported())
      {
        if (plugin.getDependencies().isEmpty() && plugin.getOptionalDependencies().isEmpty())
        {
          rootNodes.add(new PluginNode(smp));
        }
        else
        {
          appendNode(smp);
        }
      }
      else
      {
        //J-
        throw new PluginConditionFailedException(
          condition,
          String.format(
            "could not load plugin %s, the plugin condition does not match",
            plugin.getInformation().getId()
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
  /**
   * Method description
   *
   *
   * @param smp
   */
  private void appendNode(ExplodedSmp smp)
  {
    PluginNode child = new PluginNode(smp);

    for (String dependency : smp.getPlugin().getDependencies())
    {
      if (!appendNode(rootNodes, child, dependency))
      {
        //J-
        throw new PluginNotInstalledException(
          String.format(
            "dependency %s of %s is not installed",
            dependency,
            child.getId()
          )
        );
        //J+
      }
    }

    boolean rootNode = smp.getPlugin().getDependencies().isEmpty();
    for (String dependency : smp.getPlugin().getOptionalDependencies()) {
      if (appendNode(rootNodes, child, dependency)) {
        rootNode = false;
      } else {
        logger.info("optional dependency {} of {} is not installed", dependency, child.getId());
      }
    }

    if (rootNode) {
      logger.info("could not find optional dependencies of {}, append it as root node", child.getId());
      rootNodes.add(new PluginNode(smp));
    }
  }

  /**
   * Method description
   *
   *
   * @param nodes
   * @param child
   * @param dependency
   *
   * @return
   */
  private boolean appendNode(List<PluginNode> nodes, PluginNode child,
    String dependency)
  {
    logger.debug("check for {} as dependency of {}", dependency, child.getId());

    boolean found = false;

    for (PluginNode node : nodes)
    {
      if (node.getId().equals(dependency))
      {
        logger.debug("add plugin {} as child of {}", child.getId(), node.getId());
        node.addChild(child);
        found = true;

        break;
      }
      else if (appendNode(node.getChildren(), child, dependency))
      {
        found = true;

        break;
      }
    }

    return found;
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
    buffer.append(indent).append("+- ").append(node.getId()).append("\n");
    for (PluginNode child : node.getChildren()) {
      append(buffer, indent + "   ", child);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final List<PluginNode> rootNodes = Lists.newArrayList();
}
