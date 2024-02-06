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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Load web resources from a plugin webapp directory.
 *
 * @since 2.0.0
 */
public class PathWebResourceLoader implements WebResourceLoader {

  private static final String SEPARATOR = "/";

  private final Path directory;

 
  private static final Logger LOG = LoggerFactory.getLogger(PathWebResourceLoader.class);

  public PathWebResourceLoader(Path directory) {
    this.directory = directory.toAbsolutePath().normalize();
  }

  @Override
  public URL getResource(String path) {
    URL resource = null;
    Path file = directory.resolve(filePath(path)).toAbsolutePath().normalize();

    if (isValidPath(file)) {
      LOG.trace("found path {} at {}", path, file);

      try {
        resource = file.toUri().toURL();
      } catch (MalformedURLException ex) {
        LOG.error("could not transform path to url", ex);
      }
    } else {
      LOG.trace("could not find file {}", file);
    }

    return resource;
  }

  private boolean isValidPath(Path file) {
    return Files.exists(file)
      && !Files.isDirectory(file)
      && file.startsWith(directory);
  }

  private String filePath(String path) {
    if (path.startsWith(SEPARATOR)) {
      return path.substring(1);
    }
    return path;
  }

}
