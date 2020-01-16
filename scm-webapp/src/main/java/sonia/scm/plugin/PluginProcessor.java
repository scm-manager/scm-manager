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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.lifecycle.classloading.ClassLoaderLifeCycle;
import sonia.scm.plugin.ExplodedSmp.PathTransformer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 *
 * TODO don't mix nio and io
 */
@SuppressWarnings("squid:S3725") // performance is not critical, for this type of checks
public final class PluginProcessor
{

  /** Field description */
  private static final String INSTALLEDNAME_FORMAT = "%s.%03d";

  /** Field description */
  private static final String DIRECTORY_CLASSES = "classes";

  /** Field description */
  private static final String DIRECTORY_DEPENDENCIES = "lib";

  /** Field description */
  private static final String DIRECTORY_INSTALLED = ".installed";

  /** Field description */
  private static final String DIRECTORY_METAINF = "META-INF";

  /** Field description */
  private static final String DIRECTORY_WEBAPP = "webapp";

  /** Field description */
  private static final String EXTENSION_PLUGIN = ".smp";

  /** Field description */
  private static final String FORMAT_DATE = "yyyy-MM-dd";

  /** Field description */
  private static final String GLOB_JAR = "*.jar";

  /**
   * the logger for PluginProcessor
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PluginProcessor.class);

  //~--- constructors ---------------------------------------------------------

  private ClassLoaderLifeCycle classLoaderLifeCycle;

  /**
   * Constructs ...
   *
   *
   * @param classLoaderLifeCycle
   * @param pluginDirectory
   */
  public PluginProcessor(ClassLoaderLifeCycle classLoaderLifeCycle, Path pluginDirectory)
  {
    this.classLoaderLifeCycle = classLoaderLifeCycle;
    this.pluginDirectory = pluginDirectory;
    this.installedDirectory = findInstalledDirectory();

    try
    {
      this.context = JAXBContext.newInstance(InstalledPluginDescriptor.class);
    }
    catch (JAXBException ex)
    {
      throw new PluginLoadException("could not create jaxb context", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param directory
   * @param filter
   *
   * @return
   *
   * @throws IOException
   */
  private static DirectoryStream<Path> stream(Path directory,
    Filter<Path> filter)
    throws IOException
  {
    return Files.newDirectoryStream(directory, filter);
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @return
   *
   * @throws IOException
   */
  public Set<InstalledPlugin> collectPlugins(ClassLoader classLoader)
    throws IOException
  {
    logger.info("collect plugins");

    Set<Path> archives = collect(pluginDirectory, new PluginArchiveFilter());

    logger.debug("extract {} archives", archives.size());

    extract(archives);

    List<Path> dirs =
      collectPluginDirectories(pluginDirectory)
      .stream()
      .filter(isPluginDirectory())
      .collect(toList());

    logger.debug("process {} directories: {}", dirs.size(), dirs);

    List<ExplodedSmp> smps = Lists.transform(dirs, new PathTransformer());

    logger.trace("start building plugin tree");

    PluginTree pluginTree = new PluginTree(smps);

    logger.info("install plugin tree:\n{}", pluginTree);

    List<PluginNode> leafLastNodes = pluginTree.getLeafLastNodes();

    logger.trace("create plugin wrappers and build classloaders");

    Set<InstalledPlugin> wrappers = createPluginWrappers(classLoader, leafLastNodes);

    logger.debug("collected {} plugins", wrappers.size());

    return ImmutableSet.copyOf(wrappers);
  }

  private Predicate<Path> isPluginDirectory() {
    return dir -> Files.exists(dir.resolve(DIRECTORY_METAINF).resolve("scm").resolve("plugin.xml"));
  }

  /**
   * Method description
   *
   *
   * @param plugins
   * @param classLoader
   * @param node
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param directory
   * @param filter
   *
   * @return
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param parentClassLoader
   * @param smp
   *
   * @return
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @return
   */
  private String createDate()
  {
    return new SimpleDateFormat(FORMAT_DATE).format(new Date());
  }

  /**
   * Method description
   *
   *
   * @param root
   * @param parents
   *
   * @return
   */
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

      boolean core = Files.exists(directory.resolve(PluginConstants.FILE_CORE));

      ClassLoader cl = createClassLoader(classLoader, smp);

      InstalledPluginDescriptor descriptor = createDescriptor(cl, descriptorPath);

      WebResourceLoader resourceLoader = createWebResourceLoader(directory);

      plugin = new InstalledPlugin(descriptor, cl, resourceLoader, directory, core);
    } else {
      logger.warn("found plugin directory without plugin descriptor");
    }

    return plugin;
  }

  /**
   * Method description
   *
   *
   *
   * @param classLoader
   * @param nodes
   *
   * @return
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   */
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

  /**
   * Method description
   *
   *
   * @param archives
   *
   * @throws IOException
   */
  private void extract(Iterable<Path> archives) throws IOException
  {
    logger.debug("extract archives");

    for (Path archive : archives)
    {
      File archiveFile = archive.toFile();

      logger.trace("extract archive {}", archive);

      SmpArchive smp = SmpArchive.create(archive);

      logger.debug("extract plugin {}", smp.getPlugin());

      File directory =
        PluginsInternal.createPluginDirectory(pluginDirectory.toFile(),
          smp.getPlugin());

      String checksum = com.google.common.io.Files.hash(archiveFile,
                          Hashing.sha256()).toString();
      File checksumFile = PluginsInternal.getChecksumFile(directory);

      PluginsInternal.extract(smp, checksum, directory, checksumFile, false);
      moveArchive(archive);
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Path findInstalledDirectory()
  {
    Path directory = null;
    Path installed = pluginDirectory.resolve(DIRECTORY_INSTALLED);
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

  /**
   * Method description
   *
   *
   * @param archive
   *
   * @throws IOException
   */
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

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/06/04
   * @author         Enter your name here...
   */
  private static class DirectoryFilter implements DirectoryStream.Filter<Path>
  {

    /**
     * Method description
     *
     *
     * @param entry
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public boolean accept(Path entry) throws IOException
    {
      return Files.isDirectory(entry)
        &&!entry.getFileName().toString().startsWith(".");
    }
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/06/04
   * @author         Enter your name here...
   */
  private static class PluginArchiveFilter
    implements DirectoryStream.Filter<Path>
  {

    /**
     * Method description
     *
     *
     * @param entry
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public boolean accept(Path entry) throws IOException
    {
      return Files.isRegularFile(entry)
        && entry.getFileName().toString().endsWith(EXTENSION_PLUGIN);
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final JAXBContext context;

  /** Field description */
  private final Path installedDirectory;

  /** Field description */
  private final Path pluginDirectory;
}
