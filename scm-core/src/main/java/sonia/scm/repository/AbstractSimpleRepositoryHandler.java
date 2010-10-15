/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.CommandResult;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class AbstractSimpleRepositoryHandler<C extends SimpleRepositoryConfig>
        extends AbstractRepositoryHandler<C>
{

  /** Field description */
  private static final Logger logger =
    Logger.getLogger(AbstractSimpleRepositoryHandler.class.getName());

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
  protected abstract ExtendedCommand buildCreateCommand(Repository repository,
          File directory);

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

    ExtendedCommand cmd = buildCreateCommand(repository, directory);
    CommandResult result = cmd.execute();

    if (!result.isSuccessfull())
    {
      StringBuilder msg = new StringBuilder("command exit with error ");

      msg.append(result.getReturnCode()).append(" and message: '");
      msg.append(result.getOutput()).append("'");

      throw new RepositoryException(msg.toString());
    }

    postCreate(repository, directory);
    repository.setType(getType().getName());
    storeRepositoryConfig(repository);
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
        logger.log(Level.SEVERE, null, ex);
      }
    }

    return repositories;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   */
  protected void createConfigDirectory(SCMContextProvider context)
  {
    File baseDirectory = context.getBaseDirectory();

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
      if (logger.isLoggable(Level.WARNING))
      {
        logger.warning(
            "could not find repository ".concat(repository.getName()));
      }

      repository = null;
    }
    else
    {
      String url = buildUrl(repository);

      if (Util.isNotEmpty(url))
      {
        repository.setUrl(url);
      }
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
