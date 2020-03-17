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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public final class PluginNode
{

  /**
   * Constructs ...
   *
   *
   * @param plugin
   */
  public PluginNode(ExplodedSmp plugin)
  {
    this.plugin = plugin;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param node
   */
  public void addChild(PluginNode node)
  {
    this.children.add(node);
    node.addParent(this);
  }

  /**
   * Method description
   *
   *
   * @param node
   */
  private void addParent(PluginNode node)
  {
    this.parents.add(node);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  public PluginNode getChild(final String id)
  {
    return Iterables.find(children, new Predicate<PluginNode>()
    {

      @Override
      public boolean apply(PluginNode node)
      {
        return node.getId().equals(id);
      }
    });
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<PluginNode> getChildren()
  {
    return children;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return plugin.getPlugin().getInformation().getName(false);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public List<PluginNode> getParents()
  {
    return parents;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ExplodedSmp getPlugin()
  {
    return plugin;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public InstalledPlugin getWrapper()
  {
    return wrapper;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param wrapper
   */
  public void setWrapper(InstalledPlugin wrapper)
  {
    this.wrapper = wrapper;
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof PluginNode
      && ((PluginNode) obj).getId().equals(this.getId());
  }

  @Override
  public String toString() {
    return plugin.getPath().toString() + " -> " + children;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final List<PluginNode> parents = Lists.newArrayList();

  /** Field description */
  private final List<PluginNode> children = Lists.newArrayList();

  /** Field description */
  private final ExplodedSmp plugin;

  /** Field description */
  private InstalledPlugin wrapper;
}
