/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Load web resources from a plugin webapp directory.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public class PathWebResourceLoader implements WebResourceLoader
{

  private static final String SEPARATOR = "/";

  /**
   * the logger for PathWebResourceLoader
   */
  private static final Logger LOG =
    LoggerFactory.getLogger(PathWebResourceLoader.class);

  public PathWebResourceLoader(Path directory)
  {
    this.directory = directory;
  }

  @Override
  public URL getResource(String path) {
    URL resource = null;
    Path file = directory.resolve(filePath(path));

    if (Files.exists(file) && ! Files.isDirectory(file))
    {
      LOG.trace("found path {} at {}", path, file);

      try
      {
        resource = file.toUri().toURL();
      }
      catch (MalformedURLException ex)
      {
        LOG.error("could not transform path to url", ex);
      }
    } else {
      LOG.trace("could not find file {}", file);
    }

    return resource;
  }

  private String filePath(String path) {
    if (path.startsWith(SEPARATOR)) {
      return path.substring(1);
    }
    return path;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Path directory;
}
