/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.io.DirectoryFileFilter;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.11
 */
public class RepositoryUtil
{

  /** the logger for RepositoryUtil */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param configuration
   * @param repositoryManager
   * @param repository
   *
   * @since 1.16
   */
  public static void appendUrl(ScmConfiguration configuration,
                               RepositoryManager repositoryManager,
                               Repository repository)
  {
    RepositoryHandler handler =
      repositoryManager.getHandler(repository.getType());

    if (handler != null)
    {
      String url = handler.createResourcePath(repository);

      url = HttpUtil.getCompleteUrl(configuration, url);
      repository.setUrl(url);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param request
   * @param repositoryManager
   * @param repository
   *
   * @since 1.16
   */
  public static void appendUrl(HttpServletRequest request,
                               RepositoryManager repositoryManager,
                               Repository repository)
  {
    RepositoryHandler handler =
      repositoryManager.getHandler(repository.getType());

    if (handler != null)
    {
      String url = handler.createResourcePath(repository);

      url = HttpUtil.getCompleteUrl(request, url);
      repository.setUrl(url);
    }
  }

  /**
   * Method description
   *
   *
   * @param directory
   * @param names
   *
   * @return
   */
  public static List<File> searchRepositoryDirectories(File directory,
          String... names)
  {
    List<File> repositories = new ArrayList<File>();

    searchRepositoryDirectories(repositories, directory, Arrays.asList(names));

    return repositories;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param handler
   * @param directoryPath
   * @return
   *
   * @throws IOException
   */
  public static String getRepositoryName(AbstractRepositoryHandler handler,
          String directoryPath)
          throws IOException
  {
    return getRepositoryName(handler.getConfig().getRepositoryDirectory(),
                             new File(directoryPath));
  }

  /**
   * Method description
   *
   *
   *
   * @param config
   * @param directoryPath
   * @return
   *
   * @throws IOException
   */
  public static String getRepositoryName(SimpleRepositoryConfig config,
          String directoryPath)
          throws IOException
  {
    return getRepositoryName(config.getRepositoryDirectory(),
                             new File(directoryPath));
  }

  /**
   * Method description
   *
   *
   *
   * @param handler
   * @param directory
   * @return
   *
   * @throws IOException
   */
  public static String getRepositoryName(AbstractRepositoryHandler handler,
          File directory)
          throws IOException
  {
    return getRepositoryName(handler.getConfig().getRepositoryDirectory(),
                             directory);
  }

  /**
   * Method description
   *
   *
   *
   * @param config
   * @param directory
   * @return
   *
   * @throws IOException
   */
  public static String getRepositoryName(SimpleRepositoryConfig config,
          File directory)
          throws IOException
  {
    return getRepositoryName(config.getRepositoryDirectory(), directory);
  }

  /**
   * Method description
   *
   *
   *
   * @param baseDirectory
   * @param directory
   * @return
   *
   * @throws IOException
   */
  public static String getRepositoryName(File baseDirectory, File directory)
          throws IOException
  {
    String name = null;
    String path = directory.getCanonicalPath();
    int directoryLength = baseDirectory.getCanonicalPath().length();

    if (directoryLength < path.length())
    {
      name = IOUtil.trimSeperatorChars(path.substring(directoryLength));

      // replace windows path seperator
      name = name.replaceAll("\\\\", "/");
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("path is shorter as the main hg repository path");
    }

    return name;
  }

  /**
   * Method description
   *
   *
   * @param handler
   * @param directoryNames
   *
   * @return
   *
   * @throws IOException
   */
  public static List<String> getRepositoryNames(
          AbstractRepositoryHandler handler, String... directoryNames)
          throws IOException
  {
    return getRepositoryNames(handler.getConfig(), directoryNames);
  }

  /**
   * Method description
   *
   *
   * @param config
   * @param directoryNames
   *
   * @return
   *
   * @throws IOException
   */
  public static List<String> getRepositoryNames(SimpleRepositoryConfig config,
          String... directoryNames)
          throws IOException
  {
    return getRepositoryNames(config.getRepositoryDirectory(), directoryNames);
  }

  /**
   * Method description
   *
   *
   * @param baseDirectory
   * @param directoryNames
   *
   * @return
   *
   * @throws IOException
   */
  public static List<String> getRepositoryNames(File baseDirectory,
          String... directoryNames)
          throws IOException
  {
    List<String> repositories = new ArrayList<String>();
    List<File> repositoryFiles = searchRepositoryDirectories(baseDirectory,
                                   directoryNames);

    for (File file : repositoryFiles)
    {
      String name = getRepositoryName(baseDirectory, file);

      if (name != null)
      {
        repositories.add(name);
      }
    }

    return repositories;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repositories
   * @param directory
   * @param names
   */
  private static void searchRepositoryDirectories(List<File> repositories,
          File directory, List<String> names)
  {
    boolean found = false;

    for (String name : names)
    {
      if (new File(directory, name).exists())
      {
        found = true;

        break;
      }
    }

    if (found)
    {
      repositories.add(directory);
    }
    else
    {
      File[] directories = directory.listFiles(DirectoryFileFilter.instance);

      if (directories != null)
      {
        for (File d : directories)
        {
          searchRepositoryDirectories(repositories, d, names);
        }
      }
    }
  }
}
