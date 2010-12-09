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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.boot.BootstrapUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ScriptResourceServlet extends AbstractResourceServlet
{

  /** Field description */
  public static final String CONTENT_TYPE = "text/javascript";

  /** Field description */
  private static final long serialVersionUID = -5769146163848821050L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param manager
   */
  @Inject
  public ScriptResourceServlet(PluginManager manager)
  {
    this.manager = manager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stream
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void appendResources(OutputStream stream)
          throws ServletException, IOException
  {
    stream.write(
        "function sayPluginHello(){ alert('Plugin Hello !'); }".concat(
          System.getProperty("line.separator")).getBytes());

    Collection<String> scriptResources = getScriptResources();

    if (Util.isNotEmpty(scriptResources))
    {
      for (String resource : scriptResources)
      {
        appendResource(stream, resource);
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
  @Override
  protected String getContentType()
  {
    return CONTENT_TYPE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stream
   * @param script
   *
   * @throws IOException
   * @throws ServletException
   */
  private void appendResource(OutputStream stream, String script)
          throws ServletException, IOException
  {
    InputStream input = getResourceAsStream(script);

    if (input != null)
    {
      try
      {
        IOUtil.copy(input, stream);
      }
      finally
      {
        IOUtil.close(input);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param resources
   * @param plugin
   */
  private void processPlugin(Set<String> resources, Plugin plugin)
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param resource
   *
   * @return
   */
  private InputStream getResourceAsStream(String resource)
  {
    InputStream input = null;
    ClassLoader classLoader = BootstrapUtil.getClassLoader(getServletContext());

    if (classLoader != null)
    {
      String classLoaderResource = resource;

      if (classLoaderResource.startsWith("/"))
      {
        classLoaderResource = classLoaderResource.substring(1);
      }

      input = classLoader.getResourceAsStream(classLoaderResource);
    }

    if (input == null)
    {
      input = ScriptResourceServlet.class.getResourceAsStream(resource);
    }

    return input;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Collection<String> getScriptResources()
  {
    Set<String> resources = new TreeSet<String>();
    Collection<Plugin> plugins = manager.getPlugins();

    if (plugins != null)
    {
      for (Plugin plugin : plugins)
      {
        processPlugin(resources, plugin);
      }
    }

    return resources;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PluginManager manager;
}
