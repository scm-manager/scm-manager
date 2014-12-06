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

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.lang.reflect.InvocationTargetException;

import java.net.URL;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class PluginProcessorTest
{

  /** Field description */
  private static final PluginResource PLUGIN_A =
    new PluginResource("sonia/scm/plugin/scm-a-plugin.smp", "scm-a-plugin.smp",
      "sonia.scm.plugins:scm-a-plugin:1.0.0-SNAPSHOT");

  /** Field description */
  private static final PluginResource PLUGIN_B =
    new PluginResource("sonia/scm/plugin/scm-b-plugin.smp", "scm-b-plugin.smp",
      "sonia.scm.plugins:scm-b-plugin:1.0.0-SNAPSHOT");

  /** Field description */
  private static final PluginResource PLUGIN_C =
    new PluginResource("sonia/scm/plugin/scm-c-plugin.smp", "scm-c-plugin.smp",
      "sonia.scm.plugins:scm-c-plugin:1.0.0-SNAPSHOT");

  /** Field description */
  private static final PluginResource PLUGIN_D =
    new PluginResource("sonia/scm/plugin/scm-d-plugin.smp", "scm-d-plugin.smp",
      "sonia.scm.plugins:scm-d-plugin:1.0.0-SNAPSHOT");

  /** Field description */
  private static final PluginResource PLUGIN_E =
    new PluginResource("sonia/scm/plugin/scm-e-plugin.smp", "scm-e-plugin.smp",
      "sonia.scm.plugins:scm-e-plugin:1.0.0-SNAPSHOT");

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test(expected = PluginCircularDependencyException.class)
  public void testCircularDependencies() throws IOException
  {
    copySmps(PLUGIN_C, PLUGIN_D, PLUGIN_E);
    collectPlugins();
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testCollectPlugins() throws IOException
  {
    copySmp(PLUGIN_A);

    PluginWrapper plugin = collectAndGetFirst();

    assertThat(plugin.getId(), is(PLUGIN_A.id));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testCollectPluginsWithDependencies() throws IOException
  {
    copySmps(PLUGIN_A, PLUGIN_B);

    Set<PluginWrapper> plugins = collectPlugins();

    assertThat(plugins, hasSize(2));

    PluginWrapper a = findPlugin(plugins, PLUGIN_A.id);

    assertNotNull(a);

    PluginWrapper b = findPlugin(plugins, PLUGIN_B.id);

    assertNotNull(b);
  }

  /**
   * Method description
   *
   *
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InstantiationException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   */
  @Test
  public void testPluginClassLoader()
    throws IOException, ClassNotFoundException, InstantiationException,
    IllegalAccessException, NoSuchMethodException, IllegalArgumentException,
    InvocationTargetException
  {
    copySmp(PLUGIN_A);

    PluginWrapper plugin = collectAndGetFirst();
    ClassLoader cl = plugin.getClassLoader();

    // load parent class
    Class<?> clazz = cl.loadClass(PluginResource.class.getName());

    assertSame(PluginResource.class, clazz);

    // load packaged class
    clazz = cl.loadClass("sonia.scm.plugins.HelloService");
    assertNotNull(clazz);

    Object instance = clazz.newInstance();
    Object result = clazz.getMethod("sayHello").invoke(instance);

    assertEquals("hello", result);
  }

  /**
   * Method description
   *
   *
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InstantiationException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   */
  @Test
  public void testPluginClassLoaderWithDependencies()
    throws IOException, ClassNotFoundException, InstantiationException,
    IllegalAccessException, NoSuchMethodException, IllegalArgumentException,
    InvocationTargetException
  {
    copySmps(PLUGIN_A, PLUGIN_B);

    Set<PluginWrapper> plugins = collectPlugins();

    PluginWrapper plugin = findPlugin(plugins, PLUGIN_B.id);
    ClassLoader cl = plugin.getClassLoader();

    // load parent class
    Class<?> clazz = cl.loadClass(PluginResource.class.getName());

    assertSame(PluginResource.class, clazz);

    // load packaged class
    clazz = cl.loadClass("sonia.scm.plugins.HelloAgainService");
    assertNotNull(clazz);

    Object instance = clazz.newInstance();
    Object result = clazz.getMethod("sayHelloAgain").invoke(instance);

    assertEquals("hello again", result);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testPluginWebResourceLoader() throws IOException
  {
    copySmp(PLUGIN_A);

    PluginWrapper plugin = collectAndGetFirst();
    WebResourceLoader wrl = plugin.getWebResourceLoader();

    assertNotNull(wrl);

    URL url = wrl.getResource("hello");

    assertNotNull(url);
    assertThat(Resources.toString(url, Charsets.UTF_8), is("hello"));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Before
  public void setUp() throws IOException
  {
    pluginDirectory = temp.newFolder();
    processor = new PluginProcessor(pluginDirectory.toPath());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  private PluginWrapper collectAndGetFirst() throws IOException
  {
    Set<PluginWrapper> plugins = collectPlugins();

    assertThat(plugins, hasSize(1));

    return Iterables.get(plugins, 0);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  private Set<PluginWrapper> collectPlugins() throws IOException
  {
    return processor.collectPlugins(PluginProcessorTest.class.getClassLoader());
  }

  /**
   * Method description
   *
   *
   * @param plugin
   *
   * @throws IOException
   */
  private void copySmp(PluginResource plugin) throws IOException
  {
    URL resource = Resources.getResource(plugin.path);
    File file = new File(pluginDirectory, plugin.name);

    try (OutputStream out = new FileOutputStream(file))
    {
      Resources.copy(resource, out);
    }
  }

  /**
   * Method description
   *
   *
   * @param plugins
   *
   * @throws IOException
   */
  private void copySmps(PluginResource... plugins) throws IOException
  {
    for (PluginResource plugin : plugins)
    {
      copySmp(plugin);
    }
  }

  /**
   * Method description
   *
   *
   * @param plugin
   * @param id
   *
   * @return
   */
  private PluginWrapper findPlugin(Iterable<PluginWrapper> plugin,
    final String id)
  {
    return Iterables.find(plugin, new Predicate<PluginWrapper>()
    {

      @Override
      public boolean apply(PluginWrapper input)
      {
        return id.equals(input.getId());
      }
    });
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/12/06
   * @author         Enter your name here...
   */
  private static class PluginResource
  {

    /**
     * Constructs ...
     *
     *
     * @param path
     * @param name
     * @param id
     */
    public PluginResource(String path, String name, String id)
    {
      this.path = path;
      this.name = name;
      this.id = id;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final String id;

    /** Field description */
    private final String name;

    /** Field description */
    private final String path;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  /** Field description */
  private File pluginDirectory;

  /** Field description */
  private PluginProcessor processor;
}
