/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.SCMContextProvider;
import sonia.scm.io.CommandResult;
import sonia.scm.io.ExtendedCommand;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
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
public class HgRepositoryManager extends AbstractRepositoryManager
{

  /** Field description */
  public static final String TYPE_DISPLAYNAME = "Mercurial";

  /** Field description */
  public static final String TYPE_NAME = "hg";

  /** Field description */
  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME,
                                              TYPE_DISPLAYNAME);

  /** Field description */
  public static final String DEFAULT_CONFIGPATH =
    "repositories".concat(File.separator).concat(TYPE_NAME);

  /** Field description */
  public static final String CONFIG_FILE =
    "config".concat(File.separator).concat("hg.xml");

  /** Field description */
  private static final Logger logger =
    Logger.getLogger(HgRepositoryManager.class.getName());

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {}

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

    ExtendedCommand command = new ExtendedCommand(config.getHgBinary(), "init",
                                directory.getPath());
    CommandResult result = command.execute();

    if (!result.isSuccessfull())
    {
      StringBuilder msg = new StringBuilder("hg exit with error ");

      msg.append(result.getReturnCode()).append(" and message: '");
      msg.append(result.getOutput()).append("'");

      throw new RepositoryException(msg.toString());
    }

    repository.setType(TYPE_NAME);

    File hgDirectory = new File(directory, ".hg");

    writeHgrc(repository, hgDirectory);
    fireEvent(repository, RepositoryEvent.CREATE);
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

    if (new File(directory, ".hg").exists())
    {
      Util.delete(directory);
      fireEvent(repository, RepositoryEvent.DELETE);
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
    File baseDirectory = context.getBaseDirectory();

    AssertUtil.assertIsNotNull(baseDirectory);

    File configFile = new File(baseDirectory, CONFIG_FILE);

    if (configFile.exists())
    {
      config = JAXB.unmarshal(configFile, HgConfig.class);

      if (config.getConfigDirectory() == null)
      {
        File configDirectory = new File(baseDirectory, DEFAULT_CONFIGPATH);

        config.setConfigDirectory(configDirectory);
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
    File directory = getDirectory(repository);
    File hgDirectory = new File(directory, ".hg");

    if (hgDirectory.exists())
    {
      writeHgrc(repository, hgDirectory);
      fireEvent(repository, RepositoryEvent.MODIFY);
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
    File directory = getDirectory(repository);
    File hgDirectory = new File(directory, ".hg");

    if (hgDirectory.exists())
    {
      readHgrc(repository, hgDirectory);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @Override
  public Repository get(String name)
  {
    Repository repository = null;
    File directory = getDirectory(name);

    if (directory.exists() && directory.isDirectory())
    {
      File hgDirectory = new File(directory, ".hg");

      if (hgDirectory.exists() && hgDirectory.isDirectory())
      {
        repository = new Repository(TYPE_NAME, name);
        readHgrc(repository, hgDirectory);
      }
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
    String[] repositoryNames = config.getRepositoryDirectory().list();

    if ((repositoryNames != null) && (repositoryNames.length > 0))
    {
      for (String repositoryName : repositoryNames)
      {
        Repository repository = get(repositoryName);

        if (repository != null)
        {
          repositories.add(repository);
        }
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
  public boolean isConfigured()
  {
    return config != null;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param section
   * @param permissions
   */
  private void appendPermission(INISection section,
                                Collection<Permission> permissions)
  {
    HgPermissionBuilder builder = new HgPermissionBuilder(permissions);

    section.setParameter("allow_read", builder.getReadPermission());
    section.setParameter("allow_push", builder.getWritePermission());
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param hgDirectory
   */
  private void readHgrc(Repository repository, File hgDirectory)
  {
    File hgrc = new File(hgDirectory, "hgrc");

    if (hgrc.exists() && hgrc.isFile())
    {
      try
      {
        INIConfiguration iniConfig = new INIConfigurationReader().read(hgrc);
        INISection section = iniConfig.getSection("web");

        if (section != null)
        {
          repository.setDescription(section.getParameter("description"));
          repository.setContact(section.getParameter("contact"));

          String read = section.getParameter("allow_read");
          String write = section.getParameter("allow_push");

          if ((read != null) || (write != null))
          {
            if (read == null)
            {
              read = "";
            }

            if (write == null)
            {
              write = "";
            }

            List<Permission> permissions =
              new HgPermissionReader().readPermissions(read, write);

            repository.setPermissions(permissions);
          }
        }
      }
      catch (IOException ex)
      {
        logger.log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param hgDirectory
   *
   * @throws IOException
   */
  private void writeHgrc(Repository repository, File hgDirectory)
          throws IOException
  {
    INISection section = new INISection("web");

    section.setParameter("name", repository.getName());

    String description = repository.getDescription();

    if (Util.isNotEmpty(description))
    {
      section.setParameter("description", description);
    }

    String contact = repository.getContact();

    if (Util.isNotEmpty(contact))
    {
      section.setParameter("contact", contact);
    }

    Collection<Permission> permissions = repository.getPermissions();

    if (permissions != null)
    {
      appendPermission(section, permissions);
    }

    INIConfiguration iniConfig = new INIConfiguration();

    iniConfig.addSection(section);

    File hgrc = new File(hgDirectory, "hgrc");
    INIConfigurationWriter writer = new INIConfigurationWriter();

    writer.write(iniConfig, hgrc);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  private File getDirectory(String name)
  {
    return new File(config.getRepositoryDirectory(), name);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private File getDirectory(Repository repository)
  {
    return getDirectory(repository.getName());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgConfig config;
}
