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


import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.lifecycle.classloading.ClassLoaderLifeCycle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 *
 *
 * TODO don't mix nio and io
 */
@SuppressWarnings("squid:S3725") // performance is not critical, for this type of checks
public final class PluginProcessor
{

  private static final String INSTALLEDNAME_FORMAT = "%s.%03d";

  private static final String DIRECTORY_CLASSES = "classes";

  private static final String DIRECTORY_DEPENDENCIES = "lib";

  private static final String DIRECTORY_INSTALLED = ".installed";

  public static final String JAKARTA_COMPATIBLE = ".jakarta-compatible";

  private static final String DIRECTORY_METAINF = "META-INF";

  private static final String DIRECTORY_WEBAPP = "webapp";

  private static final String EXTENSION_PLUGIN = ".smp";

  private static final String FORMAT_DATE = "yyyy-MM-dd";

  private static final String GLOB_JAR = "*.jar";

 
  private static final Logger logger =
    LoggerFactory.getLogger(PluginProcessor.class);


  private final SmpDescriptorExtractor extractor = new SmpDescriptorExtractor();

  private ClassLoaderLifeCycle classLoaderLifeCycle;

  private final JAXBContext context;

  private final Path installedRootDirectory;

  private final Path installedDirectory;

  private final Path pluginDirectory;
  private final PluginArchiveCleaner pluginArchiveCleaner;

  public PluginProcessor(ClassLoaderLifeCycle classLoaderLifeCycle, Path pluginDirectory){
    this(classLoaderLifeCycle, pluginDirectory, new PluginArchiveCleaner());
  }

  @VisibleForTesting
  PluginProcessor(ClassLoaderLifeCycle classLoaderLifeCycle, Path pluginDirectory, PluginArchiveCleaner pluginArchiveCleaner) {
    this.classLoaderLifeCycle = classLoaderLifeCycle;
    this.pluginDirectory = pluginDirectory;
    this.pluginArchiveCleaner = pluginArchiveCleaner;
    this.installedRootDirectory = pluginDirectory.resolve(DIRECTORY_INSTALLED);
    this.installedDirectory = findInstalledDirectory();

    try {
      this.context = JAXBContext.newInstance(InstalledPluginDescriptor.class);
    } catch (JAXBException ex) {
      throw new PluginLoadException("could not create jaxb context", ex);
    }
  }


  private static DirectoryStream<Path> stream(Path directory,
    Filter<Path> filter)
    throws IOException
  {
    return Files.newDirectoryStream(directory, filter);
  }

  public Set<InstalledPlugin> collectPlugins(ClassLoader classLoader)
    throws IOException
  {
    logger.info("collect plugins");

    Set<ExplodedSmp> installedPlugins = findInstalledPlugins();
    logger.debug("found {} installed plugins", installedPlugins.size());

    for (ExplodedSmp installedPlugin : installedPlugins) {
      if (shouldTransform(installedPlugin)) {
        logger.debug("start jakarta transformation of already installed plugin: {}", installedPlugin.getPlugin().getInformation().getName());
        PluginTransformer.transform(installedPlugin.getPath());
        Files.createFile(installedPlugin.getPath().resolve(JAKARTA_COMPATIBLE));
      }
    }

    Set<ExplodedSmp> newlyInstalledPlugins = installPending(installedPlugins);
    logger.debug("finished installation of {} plugins", newlyInstalledPlugins.size());

    for (ExplodedSmp newInstalledSmp : newlyInstalledPlugins) {
      if (shouldTransform(newInstalledSmp)) {
        logger.debug("start jakarta transformation of newly installed smp: {}", newInstalledSmp.getPlugin().getInformation().getName());
        PluginTransformer.transform(newInstalledSmp.getPath());
        Files.createFile(newInstalledSmp.getPath().resolve(JAKARTA_COMPATIBLE));
      }
    }

    Set<ExplodedSmp> plugins = concat(installedPlugins, newlyInstalledPlugins);

    logger.trace("start building plugin tree");
    PluginTree pluginTree = new PluginTree(SCMContext.getContext().getStage(), plugins);

    logger.info("install plugin tree:\n{}", pluginTree);

    List<PluginNode> leafLastNodes = pluginTree.getLeafLastNodes();

    logger.trace("create plugin wrappers and build classloaders");

    Set<InstalledPlugin> wrappers = createPluginWrappers(classLoader, leafLastNodes);

    logger.debug("collected {} plugins", wrappers.size());

    return ImmutableSet.copyOf(wrappers);
  }

  private boolean shouldTransform(ExplodedSmp newInstalledSmp) {
    return newInstalledSmp.getPlugin().getScmVersion() < 3
      && !newInstalledSmp.getPath().resolve(JAKARTA_COMPATIBLE).toFile().exists();
  }

  private Set<ExplodedSmp> concat(Set<ExplodedSmp> installedPlugins, Set<ExplodedSmp> newlyInstalledPlugins) {
    // We first add all newly installed plugins,
    // after that we add the missing plugins, which are already installed.
    // ExplodedSmp is equal by its path, so duplicates (updates) are not in the result.
    return ImmutableSet.<ExplodedSmp>builder()
      .addAll(newlyInstalledPlugins)
      .addAll(installedPlugins)
      .build();
  }

  private Set<ExplodedSmp> installPending(Set<ExplodedSmp> installedPlugins) throws IOException {
    Set<Path> archives = collect(pluginDirectory, new PluginArchiveFilter());
    logger.debug("start installation of {} pending archives", archives.size());

    Map<Path, InstalledPluginDescriptor> pending = new HashMap<>();
    for (Path archive : archives) {
      pending.put(archive, extractor.extractPluginDescriptor(archive));
    }

    PluginInstallationContext installationContext = PluginInstallationContext.fromDescriptors(
      installedPlugins.stream().map(ExplodedSmp::getPlugin).collect(toSet()),
      pending.values()
    );

    for (Map.Entry<Path, InstalledPluginDescriptor> entry : pending.entrySet()) {
      try {
        PluginInstallationVerifier.verify(installationContext, entry.getValue());
      } catch (PluginException ex) {
        Path path = entry.getKey();
        logger.error("failed to install smp {}, because it could not be verified", path);
        logger.error("to restore scm-manager functionality remove the smp file {} from the plugin directory", path);
        throw ex;
      }
    }

    return extract(archives);
  }

  private Set<ExplodedSmp> findInstalledPlugins() throws IOException {
    return collectPluginDirectories(pluginDirectory)
      .stream()
      .filter(isPluginDirectory())
      .map(ExplodedSmp::create)
      .collect(Collectors.toSet());
  }

  private Predicate<Path> isPluginDirectory() {
    return dir -> Files.exists(dir.resolve(DIRECTORY_METAINF).resolve("scm").resolve("plugin.xml"));
  }

  private void appendPluginWrapper(Set<InstalledPlugin> plugins,
    ClassLoader classLoader, PluginNode node)
    throws IOException
  {
    if (node.getWrapper() != null) {
      return;
    }
    ExplodedSmp smp = node.getPlugin();

    List<ClassLoader> parents = Lists.newArrayList();

    for (PluginNode parent : node.getParents())
    {
      InstalledPlugin wrapper = parent.getWrapper();

      if (wrapper != null)
      {
        parents.add(wrapper.getClassLoader());
      }
      else
      {
        //J-
        throw new PluginLoadException(
          String.format(
            "parent %s of plugin %s is not ready", parent.getId(), node.getId()
          )
        );
        //J+
      }

    }

    InstalledPlugin plugin =
      createPlugin(createParentPluginClassLoader(classLoader, parents),
        smp);

    if (plugin != null)
    {
      node.setWrapper(plugin);
      plugins.add(plugin);
    }
  }

  private Set<Path> collect(Path directory, Filter<Path> filter)
    throws IOException
  {
    Set<Path> paths;

    try (DirectoryStream<Path> stream = stream(directory, filter))
    {
      paths = ImmutableSet.copyOf(stream);
    }

    return paths;
  }


  private List<Path> collectPluginDirectories(Path directory) throws IOException
  {
    Builder<Path> paths = ImmutableList.builder();

    Filter<Path> filter = new DirectoryFilter();

    try (DirectoryStream<Path> parentStream = stream(directory, filter))
    {
      for (Path parent : parentStream)
      {
        paths.add(parent);
      }
    }

    return paths.build();
  }

  private ClassLoader createClassLoader(ClassLoader parentClassLoader,
    ExplodedSmp smp)
    throws IOException
  {
    List<URL> urls = new ArrayList<>();

    Path directory = smp.getPath();

    Path metaDir = directory.resolve(DIRECTORY_METAINF);

    if (!Files.exists(metaDir))
    {
      throw new FileNotFoundException("could not find META-INF directory");
    }

    Path classesDir = directory.resolve(DIRECTORY_CLASSES);

    if (Files.exists(classesDir))
    {
      urls.add(classesDir.toUri().toURL());
    }

    Path libDir = directory.resolve(DIRECTORY_DEPENDENCIES);

    if (Files.exists(libDir))
    {
      try (DirectoryStream<Path> pathDirectoryStream = Files.newDirectoryStream(libDir, GLOB_JAR)) {
        for (Path f : pathDirectoryStream) {
          urls.add(f.toUri().toURL());
        }
      }
    }

    ClassLoader classLoader;
    URL[] urlArray = urls.toArray(new URL[urls.size()]);
    InstalledPluginDescriptor plugin = smp.getPlugin();

    String id = plugin.getInformation().getName(false);

    if (smp.getPlugin().isChildFirstClassLoader())
    {
      logger.debug("create child fist classloader for plugin {}", id);
      classLoader = classLoaderLifeCycle.createChildFirstPluginClassLoader(urlArray, parentClassLoader, id);
    }
    else
    {
      logger.debug("create parent fist classloader for plugin {}", id);
      classLoader = classLoaderLifeCycle.createPluginClassLoader(urlArray, parentClassLoader, id);
    }

    return classLoader;
  }

  
  private String createDate()
  {
    return new SimpleDateFormat(FORMAT_DATE).format(new Date());
  }


  private ClassLoader createParentPluginClassLoader(ClassLoader root,
    List<ClassLoader> parents)
  {
    ClassLoader result;
    int size = parents.size();

    if (size == 0)
    {
      result = root;
    }
    else if (size == 1)
    {
      result = parents.get(0);
    }
    else
    {
      result = new MultiParentClassLoader(parents);
    }

    return result;
  }

  private InstalledPluginDescriptor createDescriptor(ClassLoader classLoader, Path descriptor) {
    ClassLoader ctxcl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    try {
      return (InstalledPluginDescriptor) context.createUnmarshaller().unmarshal(descriptor.toFile());
    } catch (JAXBException ex) {
      throw new PluginLoadException("could not load plugin desriptor ".concat(descriptor.toString()), ex);
    } finally {
      Thread.currentThread().setContextClassLoader(ctxcl);
    }
  }

  private InstalledPlugin createPlugin(ClassLoader classLoader, ExplodedSmp smp) throws IOException {
    InstalledPlugin plugin = null;
    Path directory = smp.getPath();
    Path descriptorPath = directory.resolve(PluginConstants.FILE_DESCRIPTOR);

    if (Files.exists(descriptorPath)) {
      ClassLoader cl = createClassLoader(classLoader, smp);

      InstalledPluginDescriptor descriptor = createDescriptor(cl, descriptorPath);

      WebResourceLoader resourceLoader = createWebResourceLoader(directory);

      plugin = new InstalledPlugin(descriptor, cl, resourceLoader, directory, smp.isCore());
    } else {
      logger.warn("found plugin directory without plugin descriptor");
    }

    return plugin;
  }

  private Set<InstalledPlugin> createPluginWrappers(ClassLoader classLoader,
                                                    List<PluginNode> nodes)
    throws IOException
  {
    Set<InstalledPlugin> plugins = Sets.newHashSet();

    for (PluginNode node : nodes)
    {
      appendPluginWrapper(plugins, classLoader, node);
    }

    return plugins;
  }


  private WebResourceLoader createWebResourceLoader(Path directory)
  {
    WebResourceLoader resourceLoader;
    Path webapp = directory.resolve(DIRECTORY_WEBAPP);

    if (Files.exists(webapp))
    {
      logger.debug("create WebResourceLoader for path {}", webapp);
      resourceLoader = new PathWebResourceLoader(webapp);
    }
    else
    {
      logger.debug("create empty WebResourceLoader");
      resourceLoader = new EmptyWebResourceLoader();
    }

    return resourceLoader;
  }

  
  private Set<ExplodedSmp> extract(Iterable<Path> archives) throws IOException
  {
    logger.debug("extract archives");

    ImmutableSet.Builder<ExplodedSmp> extracted = ImmutableSet.builder();

    for (Path archive : archives)
    {
      File archiveFile = archive.toFile();

      logger.trace("extract archive {}", archive);

      SmpArchive smp = SmpArchive.create(archive);

      logger.debug("extract plugin {}", smp.getPlugin());

      File directory = PluginsInternal.createPluginDirectory(pluginDirectory.toFile(), smp.getPlugin());

      String checksum = com.google.common.io.Files.hash(archiveFile, Hashing.sha256()).toString();
      File checksumFile = PluginsInternal.getChecksumFile(directory);

      PluginsInternal.extract(smp, checksum, directory, checksumFile, false);
      moveArchive(archive);

      extracted.add(ExplodedSmp.create(directory.toPath()));
    }

    pluginArchiveCleaner.cleanup(installedRootDirectory);

    return extracted.build();
  }

  
  private Path findInstalledDirectory()
  {
    Path directory = null;
    Path installed = installedRootDirectory;
    Path date = installed.resolve(createDate());

    for (int i = 0; i < 999; i++)
    {
      Path dir = date.resolve(String.format("%03d", i));

      if (!Files.exists(dir))
      {
        directory = dir;

        break;
      }
    }

    if (directory == null)
    {
      throw new PluginException("could not find installed directory");
    }

    return directory;
  }

  
  private void moveArchive(Path archive) throws IOException
  {
    if (!Files.exists(installedDirectory))
    {
      logger.debug("create installed directory {}", installedDirectory);
      Files.createDirectories(installedDirectory);
    }

    Path installed = null;

    for (int i = 0; i < 1000; i++)
    {
      String name = String.format(INSTALLEDNAME_FORMAT, archive.getFileName(), i);

      installed = installedDirectory.resolve(name);

      if (!Files.exists(installed))
      {
        break;
      }
    }

    logger.debug("move installed archive to {}", installed);

    Files.move(archive, installed);
  }




  private static class DirectoryFilter implements DirectoryStream.Filter<Path>
  {

    @Override
    public boolean accept(Path entry) throws IOException
    {
      return Files.isDirectory(entry)
        &&!entry.getFileName().toString().startsWith(".");
    }
  }



  private static class PluginArchiveFilter
    implements DirectoryStream.Filter<Path>
  {

    @Override
    public boolean accept(Path entry) throws IOException
    {
      return Files.isRegularFile(entry)
        && entry.getFileName().toString().endsWith(EXTENSION_PLUGIN);
    }
  }

}
