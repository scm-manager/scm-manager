/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.resources;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginResources;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractResourceManager implements ResourceManager
{

  /**
   * Constructs ...
   *
   *
   *
   * @param servletContext
   * @param pluginLoader
   * @param resourceHandlers
   */
  protected AbstractResourceManager(ServletContext servletContext,
                                 PluginLoader pluginLoader,
                                 Set<ResourceHandler> resourceHandlers)
  {
    this.servletContext = servletContext;
    this.pluginLoader = pluginLoader;
    this.resourceHandlers = resourceHandlers;
    collectResources(resourceMap);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param resourceMap
   */
  protected abstract void collectResources(Map<ResourceKey,
          Resource> resourceMap);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   *
   * @return
   */
  @Override
  public Resource getResource(ResourceType type, String name)
  {
    return resourceMap.get(new ResourceKey(name, type));
  }

  /**
   * Method description
   *
   *
   * @param type
   *
   * @return
   */
  @Override
  public List<Resource> getResources(ResourceType type)
  {
    List<Resource> resources = new ArrayList<Resource>();

    for (Entry<ResourceKey, Resource> e : resourceMap.entrySet())
    {
      if (e.getKey().getType() == type)
      {
        resources.add(e.getValue());
      }
    }

    return resources;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  protected List<String> getScriptResources()
  {
    List<String> resources = new ArrayList<String>();
    Collection<Plugin> plugins = pluginLoader.getInstalledPlugins();

    if (plugins != null)
    {
      for (Plugin plugin : plugins)
      {
        processPlugin(resources, plugin);
      }
    }

    return resources;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param resources
   * @param plugin
   */
  private void processPlugin(List<String> resources, Plugin plugin)
  {
    PluginResources pluginResources = plugin.getResources();

    if (pluginResources != null)
    {
      Set<String> scriptResources = pluginResources.getScriptResources();

      if (scriptResources != null)
      {
        resources.addAll(scriptResources);
      }
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/02/03
   * @author         Enter your name here...
   */
  protected static class ResourceKey
  {

    /**
     * Constructs ...
     *
     *
     * @param name
     * @param type
     */
    public ResourceKey(String name, ResourceType type)
    {
      this.name = name;
      this.type = type;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }

      if (getClass() != obj.getClass())
      {
        return false;
      }

      final ResourceKey other = (ResourceKey) obj;

      if ((this.name == null)
          ? (other.name != null)
          : !this.name.equals(other.name))
      {
        return false;
      }

      if (this.type != other.type)
      {
        return false;
      }

      return true;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int hashCode()
    {
      int hash = 7;

      hash = 53 * hash + ((this.name != null)
                          ? this.name.hashCode()
                          : 0);
      hash = 53 * hash + ((this.type != null)
                          ? this.type.hashCode()
                          : 0);

      return hash;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public String getName()
    {
      return name;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public ResourceType getType()
    {
      return type;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String name;

    /** Field description */
    private ResourceType type;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected PluginLoader pluginLoader;

  /** Field description */
  protected Set<ResourceHandler> resourceHandlers;

  /** Field description */
  protected Map<ResourceKey, Resource> resourceMap = new HashMap<ResourceKey,
                                                       Resource>();

  /** Field description */
  protected ServletContext servletContext;
}
