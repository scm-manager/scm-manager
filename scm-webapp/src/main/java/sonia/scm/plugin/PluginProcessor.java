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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final String FILE_DESCRIPTOR =
    SmpArchive.PATH_DESCRIPTOR.substring(1);

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
   * @param parentClassLoader
   * @param directory
   *
   * @return
   *
   * @throws IOException
   */
  private static DefaultPluginClassLoader createClassLoader(
    ClassLoader parentClassLoader, Path directory)
    throws IOException
  {
    List<URL> urls = new ArrayList<>();

    Path metaDir = directory.resolve(DIRECTORY_METAINF);

    if (!Files.exists(metaDir))
    {
      throw new FileNotFoundException("could not find META-INF directory");
    }

    Path linkDir = directory.resolve(DIRECTORY_LINK);

    if (!Files.exists(linkDir))
    {
      Files.createDirectory(linkDir);
    }

    Path linkMetaDir = linkDir.resolve(DIRECTORY_METAINF);

    if (!Files.exists(linkMetaDir))
    {
      Files.deleteIfExists(linkMetaDir);
      Files.createSymbolicLink(linkMetaDir, linkMetaDir.relativize(metaDir));
    }

    urls.add(linkDir.toUri().toURL());

    Path webinfDir = directory.resolve(DIRECTORY_WEBINF);

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

    //J-
    return new DefaultPluginClassLoader(
      urls.toArray(new URL[urls.size()]),
      parentClassLoader
    );
    //J+
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

    Set<Path> directories = collectPluginDirectories(pluginDirectory);

    if (logger.isDebugEnabled())
    {
      logger.debug("process {} directories", directories.size());
    }

    Set<PluginWrapper> pluginWrappers = createPluginWrappers(classLoader,
                                          directories);

    if (logger.isDebugEnabled())
    {
      logger.debug("collected {} plugins", pluginWrappers.size());
    }

    return ImmutableSet.copyOf(pluginWrappers);
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
  private Set<Path> collectPluginDirectories(Path directory) throws IOException
  {
    Builder<Path> paths = ImmutableSet.builder();

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
   * @return
   */
  private String createDate()
  {
    return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
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
   *
   * @return
   *
   * @throws IOException
   */
  private PluginWrapper createPluginWrapper(ClassLoader classLoader,
    Path directory)
    throws IOException
  {
    PluginWrapper wrapper = null;
    Path descriptor = directory.resolve(FILE_DESCRIPTOR);

    if (Files.exists(descriptor))
    {
      ClassLoader cl = createClassLoader(classLoader, directory);

      Plugin plugin = createPlugin(cl, descriptor);

      wrapper = new PluginWrapper(plugin, cl, directory);
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
   * @param directories
   *
   * @return
   *
   * @throws IOException
   */
  private Set<PluginWrapper> createPluginWrappers(ClassLoader classLoader,
    Iterable<Path> directories)
    throws IOException
  {
    Set<PluginWrapper> plugins = Sets.newHashSet();

    for (Path directory : directories)
    {
      PluginWrapper plugin = createPluginWrapper(classLoader, directory);

      if (plugin != null)
      {
        plugins.add(plugin);
      }
    }

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

      logger.debug("extract plugin {}", smp.getPluginId());

      File directory = Plugins.createPluginDirectory(archiveFile,
                         smp.getPluginId());

      String checksum = com.google.common.io.Files.hash(archiveFile,
                          Hashing.sha256()).toString();
      File checksumFile = Plugins.getChecksumFile(directory);

      Plugins.extract(smp, checksum, directory, checksumFile, false);
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
      return Files.isDirectory(entry) &&!entry.getFileName().startsWith(".");
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
