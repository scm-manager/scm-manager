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
 */
public class SvnRepositoryHandler extends AbstractRepositoryHandler<SvnConfig>
{

  /** Field description */
  public static final String TYPE_DISPLAYNAME = "Subversion";

  /** Field description */
  public static final String TYPE_NAME = "svn";

  /** Field description */
  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME,
                                              TYPE_DISPLAYNAME);

  /** Field description */
  private static final Logger logger =
    Logger.getLogger(SvnRepositoryHandler.class.getName());

  /** Field description */
  public static final String CONFIG_DIRECTORY =
    "config".concat(File.separator).concat("svn");

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

    ExtendedCommand cmd = new ExtendedCommand(config.getSvnAdminBinary(),
                            "create", directory.getPath());
    CommandResult result = cmd.execute();

    if (!result.isSuccessfull())
    {
      StringBuilder msg = new StringBuilder("svnadmin exit with error ");

      msg.append(result.getReturnCode()).append(" and message: '");
      msg.append(result.getOutput()).append("'");

      throw new RepositoryException(msg.toString());
    }

    repository.setType(TYPE_NAME);
    JAXB.marshal(repository, getRepositoryFile(repository));
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

    File baseDirectory = context.getBaseDirectory();

    configDirectory = new File(baseDirectory, CONFIG_DIRECTORY);

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
      JAXB.marshal(repository, getRepositoryFile(repository));
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
    Repository repository = null;
    File repositoryFile = getRepositoryFile(id);

    if (repositoryFile.exists())
    {
      repository = JAXB.unmarshal(repositoryFile, Repository.class);
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
        Repository repository = JAXB.unmarshal(repositoryFile,
                                  Repository.class);

        repositories.add(repository);
      }
      catch (Exception ex)
      {
        logger.log(Level.SEVERE, null, ex);
      }
    }

    return repositories;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryType getType()
  {
    return TYPE;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Class<SvnConfig> getConfigClass()
  {
    return SvnConfig.class;
  }

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
