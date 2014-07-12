/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public final class Plugins
{

  /**
   * the logger for Plugins
   */
  private static final Logger logger = LoggerFactory.getLogger(Plugins.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private Plugins() {}

  //~--- methods --------------------------------------------------------------

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
  public static Set<PluginWrapper> collectPlugins(ClassLoader classLoader,
    Path directory)
    throws IOException
  {
    PluginProcessor processor = new PluginProcessor(directory);

    return processor.collectPlugins(classLoader);
  }

  /**
   * Method description
   *
   *
   * @param parent
   * @param id
   *
   * @return
   */
  public static File createPluginDirectory(File parent, PluginId id)
  {
    return new File(new File(parent, id.getGroupId()), id.getArtifactId());
  }

    /** Field description */
  private static final String FILE_CHECKSUM = "checksum";

  
  
  /**
   * Method description
   *
   *
   * @param archive
   * @param checksum
   * @param directory
   * @param checksumFile
   * @param core
   *
   * @throws IOException
   */
  public static void extract(SmpArchive archive, String checksum,
    File directory, File checksumFile, boolean core)
    throws IOException
  {
    if (directory.exists())
    {
      logger.debug("delete directory {} for plugin extraction",
        archive.getPluginId());
      IOUtil.delete(directory);
    }

    IOUtil.mkdirs(directory);

    logger.debug("extract plugin {}", archive.getPluginId());
    archive.extract(directory);
    Files.write(checksum, checksumFile, Charsets.UTF_8);

    if (core)
    {
      if (!new File(directory, "core").createNewFile())
      {
        throw new IOException("could not create core plugin marker");
      }
    }
  }
  
  public static File getChecksumFile(File pluginDirectory){
    return new File(pluginDirectory, FILE_CHECKSUM);
  }

  /**
   * Method description
   *
   *
   * @param wrapped
   *
   * @return
   */
  public static Iterable<Plugin> unwrap(Iterable<PluginWrapper> wrapped)
  {
    return Iterables.transform(wrapped, new Unwrap());
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/06/05
   * @author         Enter your name here...
   */
  private static class Unwrap implements Function<PluginWrapper, Plugin>
  {

    /**
     * Method description
     *
     *
     * @param wrapper
     *
     * @return
     */
    @Override
    public Plugin apply(PluginWrapper wrapper)
    {
      return wrapper.getPlugin();
    }
  }
}
