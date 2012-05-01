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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigurationException;
import sonia.scm.io.CommandResult;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.FileSystem;
import sonia.scm.store.StoreFactory;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.net.URL;

/**
 *
 * @author Sebastian Sdorra
 *
 *
 * @param <C>
 */
public abstract class AbstractSimpleRepositoryHandler<C extends SimpleRepositoryConfig>
        extends AbstractRepositoryHandler<C>
{

  /** Field description */
  public static final String DEFAULT_VERSION_INFORMATION = "unknown";

  /** Field description */
  public static final String DIRECTORY_REPOSITORY = "repositories";

  /** Field description */
  public static final String DOT = ".";

  /** the logger for AbstractSimpleRepositoryHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractSimpleRepositoryHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param storeFactory
   * @param fileSystem
   */
  public AbstractSimpleRepositoryHandler(StoreFactory storeFactory,
          FileSystem fileSystem)
  {
    super(storeFactory);
    this.fileSystem = fileSystem;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void create(Repository repository)
          throws RepositoryException, IOException
  {
    File directory = getDirectory(repository);

    if (directory.exists())
    {
      throw new RepositoryAllreadyExistExeption();
    }

    checkPath(directory);

    try
    {
      fileSystem.create(directory);
      create(repository, directory);
      postCreate(repository, directory);
    }
    catch (Exception ex)
    {
      if (directory.exists())
      {
        if (logger.isDebugEnabled())
        {
          logger.debug(
              "delete repository directory {}, because of failed repository creation",
              directory);
        }

        fileSystem.destroy(directory);
      }

      Throwables.propagateIfPossible(ex, RepositoryException.class,
                                     IOException.class);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param repository
   * @return
   */
  @Override
  public String createResourcePath(Repository repository)
  {
    StringBuilder path = new StringBuilder("/");

    path.append(getType().getName()).append("/").append(repository.getName());

    return path.toString();
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void delete(Repository repository)
          throws RepositoryException, IOException
  {
    File directory = getDirectory(repository);

    if (directory.exists())
    {
      fileSystem.destroy(directory);
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("repository {} not found", repository);
    }
  }

  /**
   * Method description
   *
   */
  @Override
  public void loadConfig()
  {
    super.loadConfig();

    if (config == null)
    {
      config = createInitialConfig();

      if (config != null)
      {
        File repositoryDirectory = config.getRepositoryDirectory();

        if (repositoryDirectory == null)
        {
          repositoryDirectory = new File(
              baseDirectory,
              DIRECTORY_REPOSITORY.concat(File.separator).concat(
                getType().getName()));
          config.setRepositoryDirectory(repositoryDirectory);
        }

        IOUtil.mkdirs(repositoryDirectory);
        storeConfig();
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void modify(Repository repository)
          throws RepositoryException, IOException
  {

    // nothing todo
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  public File getDirectory(Repository repository)
  {
    File directory = null;

    if (isConfigured())
    {
      File repositoryDirectory = config.getRepositoryDirectory();

      directory = new File(repositoryDirectory, repository.getName());

      if (!IOUtil.isChild(repositoryDirectory, directory))
      {
        StringBuilder msg = new StringBuilder(directory.getPath());

        msg.append("is not a child of ").append(repositoryDirectory.getPath());

        throw new ConfigurationException(msg.toString());
      }
    }
    else
    {
      throw new ConfigurationException("RepositoryHandler is not configured");
    }

    return directory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getVersionInformation()
  {
    return DEFAULT_VERSION_INFORMATION;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param directory
   *
   * @return
   */
  protected ExtendedCommand buildCreateCommand(Repository repository,
          File directory)
  {
    throw new UnsupportedOperationException("method is not implemented");
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param directory
   *
   * @throws IOException
   * @throws RepositoryException
   */
  protected void create(Repository repository, File directory)
          throws RepositoryException, IOException
  {
    ExtendedCommand cmd = buildCreateCommand(repository, directory);
    CommandResult result = cmd.execute();

    if (!result.isSuccessfull())
    {
      StringBuilder msg = new StringBuilder("command exit with error ");

      msg.append(result.getReturnCode()).append(" and message: '");
      msg.append(result.getOutput()).append("'");

      throw new RepositoryException(msg.toString());
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  protected C createInitialConfig()
  {
    return null;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param directory
   *
   * @throws IOException
   * @throws RepositoryException
   */
  protected void postCreate(Repository repository, File directory)
          throws IOException, RepositoryException {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the content of a classpath resource or the given default content.
   *
   *
   * @param resource path of a classpath resource
   * @param defaultContent default content to return
   *
   * @return content of a classpath resource or defaultContent
   */
  protected String getStringFromResource(String resource, String defaultContent)
  {
    String content = defaultContent;

    try
    {
      URL url = Resources.getResource(resource);

      if (url != null)
      {
        content = Resources.toString(url, Charsets.UTF_8);
      }
    }
    catch (Exception ex)
    {
      logger.error("could not read resource", ex);
    }

    return content;
  }

  /**
   * Returns true if the directory is a repository.
   *
   *
   * @param directory directory to check
   *
   * @return true if the directory is a repository
   * @since 1.9
   */
  protected boolean isRepository(File directory)
  {
    return new File(directory, DOT.concat(getType().getName())).exists();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Check path for existing repositories
   *
   *
   * @param directory repository target directory
   *
   * @throws RepositoryAllreadyExistExeption
   */
  private void checkPath(File directory) throws RepositoryAllreadyExistExeption
  {
    File repositoryDirectory = config.getRepositoryDirectory();
    File parent = directory.getParentFile();

    while ((parent != null) &&!repositoryDirectory.equals(parent))
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("check {} for existing repository", parent);
      }

      if (isRepository(parent))
      {
        if (logger.isErrorEnabled())
        {
          logger.error("parent path {} is a repository", parent);
        }

        throw new RepositoryAllreadyExistExeption();
      }

      parent = parent.getParentFile();
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private FileSystem fileSystem;
}
