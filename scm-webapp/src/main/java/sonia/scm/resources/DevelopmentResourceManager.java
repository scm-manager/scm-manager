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



package sonia.scm.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.plugin.PluginLoader;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DevelopmentResourceManager extends AbstractResourceManager
{

  /** Field description */
  public static final String PREFIX_HANDLER = "handler/";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   * @param pluginLoader
   * @param resourceHandlers
   */
  @Inject
  public DevelopmentResourceManager(ServletContext servletContext,
                                    PluginLoader pluginLoader,
                                    Set<ResourceHandler> resourceHandlers)
  {
    super(servletContext, pluginLoader, resourceHandlers);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param resourceMap
   */
  @Override
  protected void collectResources(Map<ResourceKey, Resource> resourceMap)
  {
    List<String> scripts = getScriptResources();

    for (String script : scripts)
    {
      Resource resource = new DevelopmentResource(servletContext,
                            Arrays.asList(script), Collections.EMPTY_LIST,
                            script, ResourceType.SCRIPT);

      resourceMap.put(new ResourceKey(resource.getName(), ResourceType.SCRIPT),
                      resource);
    }

    for (ResourceHandler handler : resourceHandlers)
    {
      String name = handler.getName();

      if (name.startsWith("/"))
      {
        name = name.substring(1);
      }

      name = PREFIX_HANDLER.concat(name);
      resourceMap.put(new ResourceKey(name, ResourceType.SCRIPT),
                      new DevelopmentResource(servletContext,
                        Collections.EMPTY_LIST, Arrays.asList(handler), name,
                        ResourceType.SCRIPT));
    }
  }
}
