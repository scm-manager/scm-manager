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
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.lifecycle.classloading.ClassLoaderLifeCycle;
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
public final class PluginsInternal
{

  /**
   * the logger for PluginsInternal
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PluginsInternal.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private PluginsInternal() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param classLoaderLifeCycle
   * @param directory
   *
   * @return
   *
   * @throws IOException
   */
  public static Set<InstalledPlugin> collectPlugins(ClassLoaderLifeCycle classLoaderLifeCycle,
                                                    Path directory)
    throws IOException
  {
    PluginProcessor processor = new PluginProcessor(classLoaderLifeCycle, directory);

    return processor.collectPlugins(classLoaderLifeCycle.getBootstrapClassLoader());
  }

  /**
   * Method description
   *
   *
   * @param parent
   * @param plugin
   *
   * @return
   */
  public static File createPluginDirectory(File parent, InstalledPluginDescriptor plugin)
  {
    PluginInformation info = plugin.getInformation();

    return new File(parent, info.getName());
  }

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
        archive.getPlugin().getInformation().getName(false));
      IOUtil.delete(directory);
    }

    IOUtil.mkdirs(directory);

    logger.debug("extract plugin {}",
      archive.getPlugin().getInformation().getName(false));
    archive.extract(directory);
    Files.write(checksum, checksumFile, Charsets.UTF_8);

    if (core)
    {
      if (!new File(directory, PluginConstants.FILE_CORE).createNewFile())
      {
        throw new IOException("could not create core plugin marker");
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param wrapped
   *
   * @return
   */
  public static Iterable<InstalledPluginDescriptor> unwrap(Iterable<InstalledPlugin> wrapped)
  {
    return Iterables.transform(wrapped, new Unwrap());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param pluginDirectory
   *
   * @return
   */
  public static File getChecksumFile(File pluginDirectory)
  {
    return new File(pluginDirectory, PluginConstants.FILE_CHECKSUM);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/06/05
   * @author         Enter your name here...
   */
  private static class Unwrap implements Function<InstalledPlugin, InstalledPluginDescriptor>
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
    public InstalledPluginDescriptor apply(InstalledPlugin wrapper)
    {
      return wrapper.getDescriptor();
    }
  }
}
