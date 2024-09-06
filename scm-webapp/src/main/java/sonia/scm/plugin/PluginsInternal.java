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


public final class PluginsInternal {

 
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
