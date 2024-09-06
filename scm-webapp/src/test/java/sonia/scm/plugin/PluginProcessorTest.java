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


import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import jakarta.xml.bind.JAXB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.lifecycle.classloading.ClassLoaderLifeCycle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;



@ExtendWith(MockitoExtension.class)
class PluginProcessorTest {

  private static final PluginResource PLUGIN_A =
    new PluginResource("sonia/scm/plugin/scm-a-plugin.smp", "scm-a-plugin.smp",
      "scm-a-plugin:1.0.0-SNAPSHOT");

  private static final PluginResource PLUGIN_B =
    new PluginResource("sonia/scm/plugin/scm-b-plugin.smp", "scm-b-plugin.smp",
      "scm-b-plugin:1.0.0-SNAPSHOT");

  private static final PluginResource PLUGIN_C =
    new PluginResource("sonia/scm/plugin/scm-c-plugin.smp", "scm-c-plugin.smp",
      "scm-c-plugin:1.0.0-SNAPSHOT");

  private static final PluginResource PLUGIN_D =
    new PluginResource("sonia/scm/plugin/scm-d-plugin.smp", "scm-d-plugin.smp",
      "scm-d-plugin:1.0.0-SNAPSHOT");

  private static final PluginResource PLUGIN_E =
    new PluginResource("sonia/scm/plugin/scm-e-plugin.smp", "scm-e-plugin.smp",
      "scm-e-plugin:1.0.0-SNAPSHOT");

  private static final PluginResource PLUGIN_F_1_0_0 =
    new PluginResource("sonia/scm/plugin/scm-f-plugin-1.0.0.smp",
      "scm-f-plugin.smp", "scm-f-plugin:1.0.0");

  private static final PluginResource PLUGIN_F_1_0_1 =
    new PluginResource("sonia/scm/plugin/scm-f-plugin-1.0.1.smp",
      "scm-f-plugin.smp", "scm-f-plugin:1.0.1");

  private static final PluginResource PLUGIN_THIRD_MAJOR =
    new PluginResource("sonia/scm/plugin/scm-thirdmajor-plugin.smp",
      "scm-thirdmajor-plugin.smp", "scm-thirdmajor-plugin:1.0.0");

  private static final String PLUGIN_G = "scm-g-plugin";
  private static final String PLUGIN_H = "scm-h-plugin";
  private static final String PLUGIN_I = "scm-i-plugin";

  private File pluginDirectory;
  private PluginProcessor processor;

  @Mock
  private PluginArchiveCleaner pluginArchiveCleaner;

  @BeforeEach
  void setUp(@TempDir Path tempDirectoryPath) {
    pluginDirectory = tempDirectoryPath.toFile();
    processor = new PluginProcessor(ClassLoaderLifeCycle.create(), tempDirectoryPath, pluginArchiveCleaner);
  }


  @Test
  void shouldFailOnPluginCondition() throws IOException {
    createPendingPluginInstallation(PLUGIN_G);

    assertThrows(PluginConditionFailedException.class, this::collectPlugins);
  }


  @Test
  void shouldFailOnWrongDependencyVersion() throws IOException {
    createPendingPluginInstallation(PLUGIN_H);
    createPendingPluginInstallation(PLUGIN_I);
    assertThrows(DependencyVersionMismatchException.class, this::collectPlugins);
  }

  @Test
  void shouldNotContainDuplicatesOnUpdate() throws IOException {
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

  @Test
  void shouldFailOnCircularDependencies() throws IOException {
    copySmps(PLUGIN_C, PLUGIN_D, PLUGIN_E);
    assertThrows(PluginCircularDependencyException.class, this::collectPlugins);
  }

  @Test
  void shouldCollectPlugins() throws IOException {
    copySmp(PLUGIN_A);

    InstalledPlugin plugin = collectAndGetFirst();
    assertThat(plugin.getId()).isEqualTo(PLUGIN_A.id);
  }

  @Test
  void shouldCleanupAfterCollectingPlugins() throws IOException {
    copySmp(PLUGIN_A);

    collectAndGetFirst();
    verify(pluginArchiveCleaner).cleanup(pluginDirectory.toPath().resolve(".installed"));
  }

  @Test
  void shouldCollectPluginsAndDoNotFailOnNonPluginDirectories() throws IOException {
    assertThat(new File(pluginDirectory, "some-directory").mkdirs()).isTrue();

    copySmp(PLUGIN_A);
    InstalledPlugin plugin = collectAndGetFirst();

    assertThat(plugin.getId()).isEqualTo(PLUGIN_A.id);
  }

  @Test
  void shouldCollectPluginsWithDependencies() throws IOException {
    copySmps(PLUGIN_A, PLUGIN_B);

    Set<InstalledPlugin> plugins = collectPlugins();
    assertThat(plugins).hasSize(2);

    InstalledPlugin a = findPlugin(plugins, PLUGIN_A.id);
    assertThat(a).isNotNull();

    InstalledPlugin b = findPlugin(plugins, PLUGIN_B.id);
    assertThat(b).isNotNull();
  }

  @Test
  void shouldCreateWorkingPluginClassLoader() throws Exception {
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

  @Test
  void shouldCreateWorkingPluginClassLoaderWithDependencies() throws Exception {
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

  @Test
  void shouldTransformSecondMajorPlugin() throws Exception {
    copySmps(PLUGIN_A);

    Set<InstalledPlugin> plugins = collectPlugins();

    assertThat(plugins.iterator().next().getDirectory().resolve(".jakarta-compatible")).exists();
  }

  @Test
  void shouldNotTransformThirdMajorPlugin() throws Exception {
    copySmps(PLUGIN_THIRD_MAJOR);

    Set<InstalledPlugin> plugins = collectPlugins();

    assertThat(plugins.iterator().next().getDirectory().resolve(".jakarta-compatible")).doesNotExist();
  }

  @Test
  void shouldNotTransformAlreadyTransformedPlugins() throws Exception {
    try (MockedStatic<PluginTransformer> staticMock = Mockito.mockStatic(PluginTransformer.class)) {
      copySmps(PLUGIN_A);

      collectPlugins();
      collectPlugins();
      collectPlugins();

      staticMock.verify(() -> PluginTransformer.transform(any()), times(1));
    }
  }

  @Test
  @SuppressWarnings("UnstableApiUsage")
  void shouldCreatePluginWebResourceLoader() throws IOException {
    copySmp(PLUGIN_A);

    InstalledPlugin plugin = collectAndGetFirst();
    WebResourceLoader wrl = plugin.getWebResourceLoader();
    assertThat(wrl).isNotNull();

    URL url = wrl.getResource("hello");
    assertThat(url).isNotNull();

    assertThat(Resources.toString(url, Charsets.UTF_8)).isEqualTo("hello");
  }

  @Test
  void shouldDoPluginUpdate() throws IOException {
    copySmp(PLUGIN_F_1_0_0);
    InstalledPlugin plugin = collectAndGetFirst();
    assertThat(plugin.getId()).isEqualTo(PLUGIN_F_1_0_0.id);

    copySmp(PLUGIN_F_1_0_1);
    plugin = collectAndGetFirst();
    assertThat(plugin.getId()).isEqualTo(PLUGIN_F_1_0_1.id);
  }

  private InstalledPlugin collectAndGetFirst() throws IOException {
    Set<InstalledPlugin> plugins = collectPlugins();

    assertThat(plugins).hasSize(1);

    return Iterables.get(plugins, 0);
  }

  private Set<InstalledPlugin> collectPlugins() throws IOException {
    return processor.collectPlugins(PluginProcessorTest.class.getClassLoader());
  }

  @SuppressWarnings("UnstableApiUsage")
  private void copySmp(PluginResource plugin) throws IOException {
    URL resource = Resources.getResource(plugin.path);
    File file = new File(pluginDirectory, plugin.name);

    try (OutputStream out = new FileOutputStream(file)) {
      Resources.copy(resource, out);
    }
  }

  private void copySmps(PluginResource... plugins) throws IOException {
    for (PluginResource plugin : plugins) {
      copySmp(plugin);
    }
  }

  private InstalledPlugin findPlugin(Iterable<InstalledPlugin> plugin, final String id) {
    return Iterables.find(plugin, input -> id.equals(input.getId()));
  }
  private static class PluginResource {

    private final String path;
    private final String name;
    private final String id;


    public PluginResource(String path, String name, String id) {
      this.path = path;
      this.name = name;
      this.id = id;
    }

  }

}
