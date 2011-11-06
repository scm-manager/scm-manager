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



package sonia.scm.plugin.rest;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import sonia.scm.plugin.Category;
import sonia.scm.plugin.CategoryNameComaparator;
import sonia.scm.plugin.PluginBackend;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.view.Viewable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 * @author Sebastian Sdorra
 */
@Path("/index.html")
public class OverviewResource extends ViewableResource
{

  /** Field description */
  public static final String DEFAULT_CATEGORY = "Miscellaneous";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param backend
   */
  @Inject
  public OverviewResource(ServletContext context, PluginBackend backend)
  {
    super(context);
    this.backend = backend;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @GET
  public Viewable overview()
  {
    List<Category> categories = getPluginOverview();
    Map<String, Object> vars = createVarMap("Plugin Overview");

    vars.put("categories", categories);

    return new Viewable("/index", vars);
  }

  /**
   * Method description
   *
   *
   * @param categories
   * @param plugin
   */
  private void append(Map<String, Category> categories,
                      PluginInformation plugin)
  {
    String name = plugin.getCategory();

    if (Util.isEmpty(name))
    {
      name = DEFAULT_CATEGORY;
    }

    Category category = categories.get(name);

    if (category == null)
    {
      category = new Category(name, plugin);
      categories.put(name, category);
    }

    category.getPlugins().add(plugin);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private List<Category> getPluginOverview()
  {
    List<PluginInformation> allPlugins = backend.getPlugins();

    Collections.sort(allPlugins, PluginInformationComparator.INSTANCE);

    String pid = "";
    Map<String, Category> categoryMap = new HashMap<String, Category>();

    for (PluginInformation p : allPlugins)
    {
      String currentPid = p.getGroupId().concat(":").concat(p.getArtifactId());

      if (!currentPid.equals(pid))
      {
        pid = currentPid;
        append(categoryMap, p);
      }
    }

    List<Category> categories = new ArrayList<Category>(categoryMap.values());

    Collections.sort(categories, CategoryNameComaparator.INSTANCE);

    return categories;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PluginBackend backend;
}
