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

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.lifecycle.classloading.ClassLoaderLifeCycle;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author Sebastian Sdorra
 */
public final class PluginsInternal {

  /**
   * the logger for PluginsInternal
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PluginsInternal.class);

  private PluginsInternal() {
  }

  public static Set<InstalledPlugin> collectPlugins(ClassLoaderLifeCycle classLoaderLifeCycle,
                                                    Path directory)
    throws IOException {
    PluginProcessor processor = new PluginProcessor(classLoaderLifeCycle, directory);

    return processor.collectPlugins(classLoaderLifeCycle.getBootstrapClassLoader());
  }

  public static File createPluginDirectory(File parent, InstalledPluginDescriptor plugin) {
    PluginInformation info = plugin.getInformation();

    return new File(parent, info.getName());
  }

  public static void extract(SmpArchive archive, String checksum,
                             File directory, File checksumFile, boolean core)
    throws IOException {
    if (directory.exists()) {
      logger.debug("delete directory {} for plugin extraction",
        archive.getPlugin().getInformation().getName(false));
      IOUtil.delete(directory);
    }

    IOUtil.mkdirs(directory);

    logger.debug("extract plugin {}",
      archive.getPlugin().getInformation().getName(false));
    archive.extract(directory);
    Files.write(checksum, checksumFile, Charsets.UTF_8);

    if (core) {
      if (!new File(directory, PluginConstants.FILE_CORE).createNewFile()) {
        throw new IOException("could not create core plugin marker");
      }
    }
  }

  public static Iterable<InstalledPluginDescriptor> unwrap(Iterable<InstalledPlugin> wrapped) {
    return Iterables.transform(wrapped, new Unwrap());
  }

  public static File getChecksumFile(File pluginDirectory) {
    return new File(pluginDirectory, PluginConstants.FILE_CHECKSUM);
  }

  private static class Unwrap implements Function<InstalledPlugin, InstalledPluginDescriptor> {

    @Override
    public InstalledPluginDescriptor apply(InstalledPlugin wrapper) {
      return wrapper.getDescriptor();
    }
  }
}
