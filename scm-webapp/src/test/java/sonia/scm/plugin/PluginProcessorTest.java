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

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.lifecycle.classloading.ClassLoaderLifeCycle;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class PluginProcessorTest
{

  /** Field description */
  private static final PluginResource PLUGIN_A =
    new PluginResource("sonia/scm/plugin/scm-a-plugin.smp", "scm-a-plugin.smp",
      "scm-a-plugin:1.0.0-SNAPSHOT");

  /** Field description */
  private static final PluginResource PLUGIN_B =
    new PluginResource("sonia/scm/plugin/scm-b-plugin.smp", "scm-b-plugin.smp",
      "scm-b-plugin:1.0.0-SNAPSHOT");

  /** Field description */
  private static final PluginResource PLUGIN_C =
    new PluginResource("sonia/scm/plugin/scm-c-plugin.smp", "scm-c-plugin.smp",
      "scm-c-plugin:1.0.0-SNAPSHOT");

  /** Field description */
  private static final PluginResource PLUGIN_D =
    new PluginResource("sonia/scm/plugin/scm-d-plugin.smp", "scm-d-plugin.smp",
      "scm-d-plugin:1.0.0-SNAPSHOT");

  /** Field description */
  private static final PluginResource PLUGIN_E =
    new PluginResource("sonia/scm/plugin/scm-e-plugin.smp", "scm-e-plugin.smp",
      "scm-e-plugin:1.0.0-SNAPSHOT");

  /** Field description */
  private static final PluginResource PLUGIN_F_1_0_0 =
    new PluginResource("sonia/scm/plugin/scm-f-plugin-1.0.0.smp",
      "scm-f-plugin.smp", "scm-f-plugin:1.0.0");

  /** Field description */
  private static final PluginResource PLUGIN_F_1_0_1 =
    new PluginResource("sonia/scm/plugin/scm-f-plugin-1.0.1.smp",
      "scm-f-plugin.smp", "scm-f-plugin:1.0.1");

  private static final String PLUGIN_G = "scm-g-plugin";
  private static final String PLUGIN_H = "scm-h-plugin";
  private static final String PLUGIN_I = "scm-i-plugin";

  //~--- methods --------------------------------------------------------------

  @Test(expected = PluginConditionFailedException.class)
  public void testFailedPluginCondition() throws IOException {
    createPendingPluginInstallation(PLUGIN_G);
    collectPlugins();
  }


  @Test(expected = DependencyVersionMismatchException.class)
  public void testWrongVersionOfDependency() throws IOException {
    createPendingPluginInstallation(PLUGIN_H);
    createPendingPluginInstallation(PLUGIN_I);
    collectPlugins();
  }

  @Test
  public void shouldNotContainDuplicatesOnUpdate() throws IOException {
    createInstalledPlugin("scm-mail-plugin-2-0-0");
    createInstalledPlugin("scm-review-plugin-2-0-0");
    createPendingPluginInstallation("scm-mail-plugin-2-1-0");
    createPendingPluginInstallation("scm-review-plugin-2-1-0");

    Set<String> plugins = collectPlugins().stream()
      .map(p -> p.getDescriptor().getInformation().getName(true))
      .collect(Collectors.toSet());
    assertThat(plugins).containsOnly("scm-mail-plugin:2.1.0", "scm-review-plugin:2.1.0");
  }

  @SuppressWarnings("UnstableApiUsage")
  private void createPendingPluginInstallation(String descriptorResource) throws IOException {
    URL resource = resource(descriptorResource);
    InstalledPluginDescriptor descriptor = JAXB.unmarshal(resource, InstalledPluginDescriptor.class);

    File file = new File(pluginDirectory, descriptor.getInformation().getName() + ".smp");

    try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
      zip.putNextEntry(new ZipEntry("META-INF/scm/plugin.xml"));
      Resources.copy(resource, zip);
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  private void createInstalledPlugin(String descriptorResource) throws IOException {
    URL resource = resource(descriptorResource);
    InstalledPluginDescriptor descriptor = JAXB.unmarshal(resource, InstalledPluginDescriptor.class);

    File directory = new File(pluginDirectory, descriptor.getInformation().getName());
    File scmDirectory = new File(directory, "META-INF" + File.separator + "scm");
    assertThat(scmDirectory.mkdirs()).isTrue();

    try (OutputStream output = new FileOutputStream(new File(scmDirectory, "plugin.xml"))) {
      Resources.copy(resource, output);
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  private URL resource(String descriptorResource) {
    return Resources.getResource("sonia/scm/plugin/" + descriptorResource + ".xml");
  }


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

    InstalledPlugin plugin = collectAndGetFirst();

    assertThat(plugin.getId()).isEqualTo(PLUGIN_A.id);
  }

  @Test
  public void shouldCollectPluginsAndDoNotFailOnNonPluginDirectories() throws IOException {
    new File(pluginDirectory, "some-directory").mkdirs();

    copySmp(PLUGIN_A);
    InstalledPlugin plugin = collectAndGetFirst();

    assertThat(plugin.getId()).isEqualTo(PLUGIN_A.id);
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

    Set<InstalledPlugin> plugins = collectPlugins();
    assertThat(plugins).hasSize(2);

    InstalledPlugin a = findPlugin(plugins, PLUGIN_A.id);
    assertThat(a).isNotNull();

    InstalledPlugin b = findPlugin(plugins, PLUGIN_B.id);
    assertThat(b).isNotNull();
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

    InstalledPlugin plugin = collectAndGetFirst();
    ClassLoader cl = plugin.getClassLoader();

    // load parent class
    Class<?> clazz = cl.loadClass(PluginResource.class.getName());

    assertThat(PluginResource.class).isSameAs(clazz);

    // load packaged class
    clazz = cl.loadClass("sonia.scm.plugins.HelloService");
    assertThat(clazz).isNotNull();

    Object instance = clazz.newInstance();
    Object result = clazz.getMethod("sayHello").invoke(instance);

    assertThat(result).isEqualTo("hello");
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

    Set<InstalledPlugin> plugins = collectPlugins();

    InstalledPlugin plugin = findPlugin(plugins, PLUGIN_B.id);
    ClassLoader cl = plugin.getClassLoader();

    // load parent class
    Class<?> clazz = cl.loadClass(PluginResource.class.getName());

    assertThat(PluginResource.class).isSameAs(clazz);

    // load packaged class
    clazz = cl.loadClass("sonia.scm.plugins.HelloAgainService");
    assertThat(clazz).isNotNull();

    Object instance = clazz.newInstance();
    Object result = clazz.getMethod("sayHelloAgain").invoke(instance);

    assertThat(result).isEqualTo("hello again");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  @SuppressWarnings("UnstableApiUsage")
  public void testPluginWebResourceLoader() throws IOException
  {
    copySmp(PLUGIN_A);

    InstalledPlugin plugin = collectAndGetFirst();
    WebResourceLoader wrl = plugin.getWebResourceLoader();
    assertThat(wrl).isNotNull();

    URL url = wrl.getResource("hello");
    assertThat(url).isNotNull();

    assertThat(Resources.toString(url, Charsets.UTF_8)).isEqualTo("hello");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testUpdate() throws IOException
  {
    copySmp(PLUGIN_F_1_0_0);
    InstalledPlugin plugin = collectAndGetFirst();
    assertThat(plugin.getId()).isEqualTo(PLUGIN_F_1_0_0.id);

    copySmp(PLUGIN_F_1_0_1);
    plugin = collectAndGetFirst();
    assertThat(plugin.getId()).isEqualTo(PLUGIN_F_1_0_1.id);
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
    processor = new PluginProcessor(ClassLoaderLifeCycle.create(), pluginDirectory.toPath());
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
  private InstalledPlugin collectAndGetFirst() throws IOException
  {
    Set<InstalledPlugin> plugins = collectPlugins();

    assertThat(plugins).hasSize(1);

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
  private Set<InstalledPlugin> collectPlugins() throws IOException
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
  @SuppressWarnings("UnstableApiUsage")
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
  private InstalledPlugin findPlugin(Iterable<InstalledPlugin> plugin,
                                     final String id)
  {
    return Iterables.find(plugin, input -> id.equals(input.getId()));
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
