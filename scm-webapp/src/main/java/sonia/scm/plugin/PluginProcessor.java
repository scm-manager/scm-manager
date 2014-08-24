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

import sonia.scm.plugin.ExplodedSmp.PathTransformer;

//~--- JDK imports ------------------------------------------------------------

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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 *
 * TODO don't mix nio and io
 */
public final class PluginProcessor
{

  /** Field description */
  private static final String DIRECTORY_CLASSES = "classes";

  /** Field description */
  private static final String DIRECTORY_DEPENDENCIES = "lib";

  /** Field description */
  private static final String DIRECTORY_INSTALLED = ".installed";

  /** Field description */
  private static final String DIRECTORY_LINK = ".link";

  /** Field description */
  private static final String DIRECTORY_METAINF = "META-INF";

  /** Field description */
  private static final String DIRECTORY_WEBINF = "WEB-INF";

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

  /**
   * Constructs ...
   *
   *
   * @param pluginDirectory
   */
  public PluginProcessor(Path pluginDirectory)
  {
    this.pluginDirectory = pluginDirectory;
    this.installedDirectory = findInstalledDirectory();

    try
    {
      this.context = JAXBContext.newInstance(Plugin.class);
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
  public Set<PluginWrapper> collectPlugins(ClassLoader classLoader)
    throws IOException
  {
    logger.info("collect plugins");

    Set<Path> archives = collect(pluginDirectory, new PluginArchiveFilter());

    if (logger.isDebugEnabled())
    {
      logger.debug("extract {} archives", archives.size());
    }

    extract(archives);

    List<Path> dirs = collectPluginDirectories(pluginDirectory);

    if (logger.isDebugEnabled())
    {
      logger.debug("process {} directories", dirs.size());
    }

    List<ExplodedSmp> smps = Lists.transform(dirs, new PathTransformer());

    logger.trace("start building plugin tree");

    List<PluginNode> rootNodes = new PluginTree(smps).getRootNodes();

    logger.trace("create plugin wrappers and build classloaders");

    Set<PluginWrapper> wrappers = createPluginWrappers(classLoader, rootNodes);

    if (logger.isDebugEnabled())
    {
      logger.debug("collected {} plugins", wrappers.size());
    }

    return ImmutableSet.copyOf(wrappers);
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
  private void appendPluginWrapper(Set<PluginWrapper> plugins,
    ClassLoader classLoader, PluginNode node)
    throws IOException
  {
    ExplodedSmp smp = node.getPlugin();

    List<ClassLoader> parents = Lists.newArrayList();

    for (PluginNode parent : node.getParents())
    {
      PluginWrapper wrapper = parent.getWrapper();

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

    PluginWrapper plugin =
      createPluginWrapper(createParentPluginClassLoader(classLoader, parents),
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
   * @param plugins
   * @param classLoader
   * @param nodes
   *
   * @throws IOException
   */
  private void appendPluginWrappers(Set<PluginWrapper> plugins,
    ClassLoader classLoader, List<PluginNode> nodes)
    throws IOException
  {

    // TODO fix plugin loading order
    for (PluginNode node : nodes)
    {
      appendPluginWrapper(plugins, classLoader, node);
    }

    for (PluginNode node : nodes)
    {
      appendPluginWrappers(plugins, classLoader, node.getChildren());
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
        try (DirectoryStream<Path> direcotries = stream(parent, filter))
        {
          paths.addAll(direcotries);
        }
      }
    }

    return paths.build();
  }

  /**
   * Method description
   *
   *
   * @param parentClassLoader
   * @param directory
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

    Path metaDir = smp.getPath().resolve(DIRECTORY_METAINF);

    if (!Files.exists(metaDir))
    {
      throw new FileNotFoundException("could not find META-INF directory");
    }

    Path webinfDir = smp.getPath().resolve(DIRECTORY_WEBINF);

    if (Files.exists(webinfDir))
    {
      Path classesDir = webinfDir.resolve(DIRECTORY_CLASSES);

      if (Files.exists(classesDir))
      {
        urls.add(classesDir.toUri().toURL());
      }

      Path libDir = webinfDir.resolve(DIRECTORY_DEPENDENCIES);

      if (Files.exists(libDir))
      {
        for (Path f : Files.newDirectoryStream(libDir, GLOB_JAR))
        {
          urls.add(f.toUri().toURL());
        }
      }
    }

    ClassLoader classLoader;
    URL[] urlArray = urls.toArray(new URL[urls.size()]);
    Plugin plugin = smp.getPlugin();

    if (smp.getPlugin().isChildFirstClassLoader())
    {
      logger.debug("create child fist classloader for plugin {}",
        plugin.getInformation().getId());
      classLoader = new ChildFirstPluginClassLoader(urlArray,
        parentClassLoader);
    }
    else
    {
      logger.debug("create parent fist classloader for plugin {}",
        plugin.getInformation().getId());
      classLoader = new DefaultPluginClassLoader(urlArray, parentClassLoader);
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

  /**
   * Method description
   *
   *
   *
   * @param classLoader
   * @param descriptor
   *
   * @return
   */
  private Plugin createPlugin(ClassLoader classLoader, Path descriptor)
  {
    ClassLoader ctxcl = Thread.currentThread().getContextClassLoader();

    Thread.currentThread().setContextClassLoader(classLoader);

    try
    {
      return (Plugin) context.createUnmarshaller().unmarshal(
        descriptor.toFile());
    }
    catch (JAXBException ex)
    {
      throw new PluginLoadException(
        "could not load plugin desriptor ".concat(descriptor.toString()), ex);
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(ctxcl);
    }
  }

  /**
   * Method description
   *
   *
   * @param classLoader
   * @param directory
   * @param smp
   *
   * @return
   *
   * @throws IOException
   */
  private PluginWrapper createPluginWrapper(ClassLoader classLoader,
    ExplodedSmp smp)
    throws IOException
  {
    PluginWrapper wrapper = null;
    Path descriptor = smp.getPath().resolve(PluginConstants.FILE_DESCRIPTOR);

    if (Files.exists(descriptor))
    {
      ClassLoader cl = createClassLoader(classLoader, smp);

      Plugin plugin = createPlugin(cl, descriptor);

      wrapper = new PluginWrapper(plugin, cl, smp.getPath());
    }
    else
    {
      logger.warn("found plugin directory without plugin descriptor");
    }

    return wrapper;
  }

  /**
   * Method description
   *
   *
   *
   * @param classLoader
   * @param smps
   * @param rootNodes
   *
   * @return
   *
   * @throws IOException
   */
  private Set<PluginWrapper> createPluginWrappers(ClassLoader classLoader,
    List<PluginNode> rootNodes)
    throws IOException
  {
    Set<PluginWrapper> plugins = Sets.newHashSet();

    appendPluginWrappers(plugins, classLoader, rootNodes);

    return plugins;
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

    Files.move(archive, installedDirectory.resolve(archive.getFileName()));
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
