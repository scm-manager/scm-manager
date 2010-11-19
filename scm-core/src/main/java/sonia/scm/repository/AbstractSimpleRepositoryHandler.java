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

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.CommandResult;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXB;

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
  public static final String DIRECTORY_REPOSITORY = "repositories";

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractSimpleRepositoryHandler.class);

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
    initNewRepository(repository);

    File directory = getDirectory(repository);

    if (directory.exists())
    {
      throw new RepositoryAllreadyExistExeption();
    }

    create(repository, directory);
    postCreate(repository, directory);
    repository.setType(getType().getName());
    storeRepositoryConfig(repository);
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
    File repositoryFile = getRepositoryFile(repository);

    if (directory.exists() && repositoryFile.exists())
    {
      IOUtil.delete(directory);
      IOUtil.delete(repositoryFile);
    }
    else
    {
      throw new RepositoryException("repository does not exists");
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    super.init(context);
    createConfigDirectory(context);
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
    Repository old = get(repository.getId());

    if (old.getName().equals(repository.getName()))
    {
      storeRepositoryConfig(repository);
    }
    else
    {
      throw new RepositoryException(
          "the name of a repository could not changed");
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
  public void refresh(Repository repository)
          throws RepositoryException, IOException
  {
    Repository fresh = get(repository.getId());

    fresh.copyProperties(repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public Repository get(String id)
  {
    File repositoryFile = getRepositoryFile(id);
    Repository repository = null;

    if (repositoryFile.exists())
    {
      repository = getRepositoryFromConfig(repositoryFile);
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Repository> getAll()
  {
    List<Repository> repositories = new ArrayList<Repository>();
    File[] repositoryFiles = configDirectory.listFiles(new FilenameFilter()
    {
      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".xml");
      }
    });

    for (File repositoryFile : repositoryFiles)
    {
      try
      {
        Repository repository = getRepositoryFromConfig(repositoryFile);

        if (repository != null)
        {
          repositories.add(repository);
        }
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }

    return repositories;
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
   * @param context
   */
  protected void createConfigDirectory(SCMContextProvider context)
  {
    configDirectory =
      new File(baseDirectory,
               "config".concat(File.separator).concat(getType().getName()));

    if (!configDirectory.exists() &&!configDirectory.mkdirs())
    {
      throw new ConfigurationException("could not create config directory");
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

  /**
   * Method description
   *
   *
   * @param repository
   */
  protected void storeRepositoryConfig(Repository repository)
  {
    JAXB.marshal(repository, getRepositoryFile(repository));
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
  protected File getDirectory(Repository repository)
  {
    File directory = null;

    if (isConfigured())
    {
      directory = new File(config.getRepositoryDirectory(),
                           repository.getName());
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
   * @param repositoryFile
   *
   * @return
   */
  protected Repository getRepositoryFromConfig(File repositoryFile)
  {
    Repository repository = JAXB.unmarshal(repositoryFile, Repository.class);
    File directory = getDirectory(repository);

    if (!directory.exists())
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("could not find repository ".concat(repository.getName()));
      }

      repository = null;
    }

    return repository;
  }

  /**
   * Method description
   *
   *
   *
   * @param id
   *
   * @return
   */
  private File getRepositoryFile(String id)
  {
    return new File(configDirectory, id.concat(".xml"));
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private File getRepositoryFile(Repository repository)
  {
    return getRepositoryFile(repository.getId());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File configDirectory;
}
