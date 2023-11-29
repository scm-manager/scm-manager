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

package sonia.scm.lifecycle;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import jakarta.servlet.ServletContext;
import jakarta.xml.bind.DataBindingException;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.lifecycle.classloading.ClassLoaderLifeCycle;
import sonia.scm.migration.UpdateException;
import sonia.scm.plugin.ConfigurationResolver;
import sonia.scm.plugin.DefaultPluginLoader;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginException;
import sonia.scm.plugin.PluginLoadException;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginTransformException;
import sonia.scm.plugin.PluginTransformer;
import sonia.scm.plugin.PluginsInternal;
import sonia.scm.plugin.SmpArchive;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class PluginBootstrap {

  private static final Logger LOG = LoggerFactory.getLogger(PluginBootstrap.class);

  private static final String DIRECTORY_PLUGINS = "plugins";
  private static final String PLUGIN_DIRECTORY = "/WEB-INF/plugins/";
  private static final String PLUGIN_COREINDEX = PLUGIN_DIRECTORY.concat("plugin-index.xml");

  private final ClassLoaderLifeCycle classLoaderLifeCycle;
  private final ConfigurationResolver configurationResolver;
  private final ServletContext servletContext;
  private final Set<InstalledPlugin> plugins;
  private final PluginLoader pluginLoader;

  PluginBootstrap(ServletContext servletContext, ClassLoaderLifeCycle classLoaderLifeCycle, ConfigurationResolver configurationResolver) {
    this.servletContext = servletContext;
    this.classLoaderLifeCycle = classLoaderLifeCycle;
    this.configurationResolver = configurationResolver;

    this.plugins = collectPlugins();
    this.pluginLoader = createPluginLoader();
  }

  public PluginLoader getPluginLoader() {
    return pluginLoader;
  }

  public Set<InstalledPlugin> getPlugins() {
    return plugins;
  }

  private PluginLoader createPluginLoader() {
    return new DefaultPluginLoader(servletContext, classLoaderLifeCycle.getBootstrapClassLoader(), plugins, configurationResolver);
  }

  private Set<InstalledPlugin> collectPlugins() {
    try {
      File pluginDirectory = getPluginDirectory();

      renameOldPluginsFolder(pluginDirectory);

      if (!isCorePluginExtractionDisabled()) {
        extractCorePlugins(servletContext, pluginDirectory);
      } else {
        LOG.info("core plugin extraction is disabled");
      }
      uninstallMarkedPlugins(pluginDirectory.toPath());
      transformIncompatiblePlugins(pluginDirectory.toPath());
      return PluginsInternal.collectPlugins(classLoaderLifeCycle, pluginDirectory.toPath());
    } catch (IOException ex) {
      throw new PluginLoadException("could not load plugins", ex);
    }
  }

  private void transformIncompatiblePlugins(Path pluginsDirectory) {
    try (Stream<Path> list = java.nio.file.Files.list(pluginsDirectory)) {
      list
        .filter(java.nio.file.Files::isDirectory)
        .filter(this::isIncompatiblePlugin)
        .forEach(plugin -> {
          PluginTransformer.transform(plugin);
          try {
            Files.touch(plugin.resolve(InstalledPlugin.COMPATIBILITY_MARKER_FILENAME).toFile());
          } catch (IOException e) {
            throw new PluginTransformException("Failed to create marker file for jakarta compatibility", e);
          }
        });
    } catch (IOException e) {
      LOG.warn("error occurred while checking for plugins that should be transformed", e);
    }
  }

  private boolean isIncompatiblePlugin(Path path) {
      return !new File(path.toFile(), InstalledPlugin.COMPATIBILITY_MARKER_FILENAME).exists();
  }

  private void uninstallMarkedPlugins(Path pluginDirectory) {
    try (Stream<Path> list = java.nio.file.Files.list(pluginDirectory)) {
      list
        .filter(java.nio.file.Files::isDirectory)
        .filter(this::isMarkedForUninstall)
        .forEach(this::uninstall);
    } catch (IOException e) {
      LOG.warn("error occurred while checking for plugins that should be uninstalled", e);
    }
  }

  private boolean isMarkedForUninstall(Path path) {
    return java.nio.file.Files.exists(path.resolve(InstalledPlugin.UNINSTALL_MARKER_FILENAME));
  }

  private void uninstall(Path path) {
    try {
      LOG.info("deleting plugin directory {}", path);
      IOUtil.delete(path.toFile());
    } catch (IOException e) {
      LOG.warn("could not delete plugin directory {}", path, e);
    }
  }

  private void renameOldPluginsFolder(File pluginDirectory) {
    if (new File(pluginDirectory, "classpath.xml").exists()) {
      File backupDirectory = new File(pluginDirectory.getParentFile(), "plugins.v1");
      boolean renamed = pluginDirectory.renameTo(backupDirectory);
      if (renamed) {
        LOG.warn("moved old plugins directory to {}", backupDirectory);
      } else {
        throw new UpdateException("could not rename existing v1 plugin directory");
      }
    }
  }

  private boolean isCorePluginExtractionDisabled() {
    return Boolean.getBoolean("sonia.scm.boot.disable-core-plugin-extraction");
  }

  private void extractCorePlugin(ServletContext context, File pluginDirectory,
                                 PluginIndexEntry entry) throws IOException {
    URL url = context.getResource(PLUGIN_DIRECTORY.concat(entry.getName()));
    SmpArchive archive = SmpArchive.create(url);
    InstalledPluginDescriptor plugin = archive.getPlugin();

    File directory = PluginsInternal.createPluginDirectory(pluginDirectory, plugin);
    File checksumFile = PluginsInternal.getChecksumFile(directory);

    if (!directory.exists()) {
      LOG.warn("install plugin {}", plugin.getInformation().getId());
      PluginsInternal.extract(archive, entry.getChecksum(), directory, checksumFile, true);
    } else if (!checksumFile.exists()) {
      LOG.warn("plugin directory {} exists without checksum file.", directory);
      PluginsInternal.extract(archive, entry.getChecksum(), directory, checksumFile, true);
    } else {
      String checksum = Files.toString(checksumFile, Charsets.UTF_8).trim();

      if (checksum.equals(entry.getChecksum())) {
        LOG.debug("plugin {} is up to date", plugin.getInformation().getId());
      } else {
        LOG.warn("checksum mismatch of pluing {}, start update", plugin.getInformation().getId());
        PluginsInternal.extract(archive, entry.getChecksum(), directory, checksumFile, true);
      }
    }
  }

  private void extractCorePlugins(ServletContext context, File pluginDirectory) throws IOException {
    IOUtil.mkdirs(pluginDirectory);

    PluginIndex index = readCorePluginIndex(context);

    for (PluginIndexEntry entry : index) {
      extractCorePlugin(context, pluginDirectory, entry);
    }
  }


  private PluginIndex readCorePluginIndex(ServletContext context) {
    PluginIndex index;

    try {
      URL indexUrl = context.getResource(PLUGIN_COREINDEX);

      if (indexUrl == null) {
        throw new PluginException("no core plugin index found");
      }

      index = JAXB.unmarshal(indexUrl, PluginIndex.class);
    } catch (MalformedURLException ex) {
      throw new PluginException("could not load core plugin index", ex);
    } catch (DataBindingException ex) {
      throw new PluginException("could not unmarshal core plugin index", ex);
    }

    return index;
  }

  private File getPluginDirectory() {
    File baseDirectory = SCMContext.getContext().getBaseDirectory();

    return new File(baseDirectory, DIRECTORY_PLUGINS);
  }


  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "plugin-index")
  private static class PluginIndex implements Iterable<PluginIndexEntry> {

    @XmlElement(name = "plugins")
    private List<PluginIndexEntry> plugins;

    @Override
    public Iterator<PluginIndexEntry> iterator() {
      return getPlugins().iterator();
    }

    public List<PluginIndexEntry> getPlugins() {
      if (plugins == null) {
        plugins = ImmutableList.of();
      }

      return plugins;
    }

  }

  @XmlRootElement(name = "plugins")
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class PluginIndexEntry {

    private String checksum;

    private String name;

    public String getName() {
      return name;
    }

    public String getChecksum() {
      return checksum;
    }

  }

}
