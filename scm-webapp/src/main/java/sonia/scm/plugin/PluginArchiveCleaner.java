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
