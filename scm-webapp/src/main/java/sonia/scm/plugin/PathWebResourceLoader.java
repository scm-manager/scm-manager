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

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

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

  /** Field description */
  private static final String DEFAULT_SEPARATOR = "/";

  /**
   * the logger for PathWebResourceLoader
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PathWebResourceLoader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param directory
   */
  public PathWebResourceLoader(Path directory)
  {
    this.directory = directory;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  @Override
  public URL getResource(String path)
  {
    URL resource = null;
    Path file = directory.resolve(filePath(path));

    if (Files.exists(file))
    {
      logger.trace("found path {} at {}", path, file);

      try
      {
        resource = file.toUri().toURL();
      }
      catch (MalformedURLException ex)
      {
        logger.error("could not transform path to url", ex);
      }
    }

    return resource;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private String filePath(String path)
  {

    // TODO handle illegal path parts, such as ..
    String filePath = filePath(DEFAULT_SEPARATOR, path);

    if (!DEFAULT_SEPARATOR.equals(File.separator))
    {
      filePath = filePath(File.separator, path);
    }

    return filePath;
  }

  /**
   * Method description
   *
   *
   * @param separator
   * @param path
   *
   * @return
   */
  private String filePath(String separator, String path)
  {
    String filePath = path;

    if (filePath.startsWith(separator))
    {
      filePath = filePath.substring(separator.length());
    }

    return filePath;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Path directory;
}
