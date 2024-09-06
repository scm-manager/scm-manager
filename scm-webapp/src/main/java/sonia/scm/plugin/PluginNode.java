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


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;


public final class PluginNode
{

 
  public PluginNode(ExplodedSmp plugin)
  {
    this.plugin = plugin;
  }



  public void addChild(PluginNode node)
  {
    this.children.add(node);
    node.addParent(this);
  }


  private void addParent(PluginNode node)
  {
    this.parents.add(node);
  }



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

  
  public List<PluginNode> getChildren()
  {
    return children;
  }

  
  public String getId()
  {
    return plugin.getPlugin().getInformation().getName(false);
  }

  
  public List<PluginNode> getParents()
  {
    return parents;
  }

  
  public ExplodedSmp getPlugin()
  {
    return plugin;
  }

  
  public InstalledPlugin getWrapper()
  {
    return wrapper;
  }



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

  private final List<PluginNode> parents = Lists.newArrayList();

  private final List<PluginNode> children = Lists.newArrayList();

  private final ExplodedSmp plugin;

  private InstalledPlugin wrapper;
}
