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
