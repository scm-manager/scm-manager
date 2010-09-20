/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.group.GroupManager;
import sonia.scm.repository.BasicRepositoryManager;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.security.MessageDigestEncryptionHandler;
import sonia.scm.util.ServiceUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class BasicContextProvider implements SCMContextProvider
{

  /** Field description */
  public static final String DIRECTORY_DEFAULT = ".scm";

  /** Field description */
  public static final String DIRECTORY_ENVIRONMENT = "SCM_HOME";

  /** Field description */
  public static final String DIRECTORY_PROPERTY = "scm.home";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public BasicContextProvider()
  {
    baseDirectory = findBaseDirectory();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    for (GroupManager manager : groupManagerMap.values())
    {
      manager.close();
    }

    repositoryManager.close();
  }

  /**
   * Method description
   *
   */
  @Override
  public void init()
  {
    loadGroupManagers();
    loadRepositoryManager();
    loadEncryptionHandler();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public File getBaseDirectory()
  {
    return baseDirectory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public EncryptionHandler getEncryptionHandler()
  {
    return encryptionHandler;
  }

  /**
   * Method description
   *
   *
   * @param type
   *
   * @return
   */
  @Override
  public GroupManager getGroupManager(String type)
  {
    return groupManagerMap.get(type);
  }

  /**
   * Method description
   *
   *
   *
   * @return
   */
  @Override
  public RepositoryManager getRepositoryManager()
  {
    return repositoryManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private File findBaseDirectory()
  {
    String path = System.getProperty(DIRECTORY_PROPERTY);

    if (Util.isEmpty(path))
    {
      path = System.getenv(DIRECTORY_ENVIRONMENT);

      if (Util.isEmpty(path))
      {
        path = System.getProperty("user.home").concat(File.separator).concat(
          DIRECTORY_DEFAULT);
      }
    }

    File directory = new File(path);

    if (!directory.exists() &&!directory.mkdirs())
    {
      throw new IllegalStateException("could not create directory");
    }

    return directory;
  }

  /**
   * Method description
   *
   */
  private void loadEncryptionHandler()
  {
    encryptionHandler = ServiceUtil.getService(EncryptionHandler.class);

    if (encryptionHandler == null)
    {
      encryptionHandler = new MessageDigestEncryptionHandler();
    }
  }

  /**
   * Method description
   *
   */
  private void loadGroupManagers()
  {
    groupManagerMap = new HashMap<String, GroupManager>();

    List<GroupManager> groupManagers =
      ServiceUtil.getServices(GroupManager.class);

    for (GroupManager manager : groupManagers)
    {
      manager.init(this);
      groupManagerMap.put(manager.getType(), manager);
    }
  }

  /**
   * Method description
   *
   */
  private void loadRepositoryManager()
  {
    repositoryManager = ServiceUtil.getService(RepositoryManager.class);

    if (repositoryManager == null)
    {
      repositoryManager = new BasicRepositoryManager();
    }

    repositoryManager.init(this);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;

  /** Field description */
  private EncryptionHandler encryptionHandler;

  /** Field description */
  private Map<String, GroupManager> groupManagerMap;

  /** Field description */
  private RepositoryManager repositoryManager;
}
