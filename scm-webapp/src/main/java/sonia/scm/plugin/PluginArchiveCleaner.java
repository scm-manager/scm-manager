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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginArchiveCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(PluginArchiveCleaner.class);
  private static final int MAX_ARCHIVE_COUNT = 5;

  public void cleanup(Path archivePath) throws IOException {

    if (Files.exists(archivePath)) {
      try (Stream<Path> pathStream = Files.list(archivePath)) {
        List<Path> pathList = pathStream
          .filter(PluginArchiveCleaner::isAnInstalledPluginDirectory)
          .sorted()
          .collect(Collectors.toList());

        for (int i = 0; i <= pathList.size() - MAX_ARCHIVE_COUNT; i++) {
          LOG.debug("Delete old installation directory {}", pathList.get(i));
          IOUtil.deleteSilently(pathList.get(i).toFile());
        }
      }
    }
  }

  private static boolean isAnInstalledPluginDirectory(Path path) {
    return Files.isDirectory(path) && path.getFileName().toString().matches("\\d\\d\\d\\d-\\d\\d-\\d\\d");
  }
}
