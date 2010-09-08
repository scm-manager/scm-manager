/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.group.GroupManager;
import sonia.scm.repository.RepositoryManager;
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

    for (RepositoryManager manager : repositoryManagerMap.values())
    {
      manager.close();
    }
  }

  /**
   * Method description
   *
   */
  @Override
  public void init()
  {
    loadGroupManagers();
    loadRepositoryManagers();
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
   * @param type
   *
   * @return
   */
  @Override
  public RepositoryManager getRepositoryManager(String type)
  {
    return repositoryManagerMap.get(type);
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
  private void loadRepositoryManagers()
  {
    repositoryManagerMap = new HashMap<String, RepositoryManager>();

    List<RepositoryManager> repositoryManagers =
      ServiceUtil.getServices(RepositoryManager.class);

    for (RepositoryManager manager : repositoryManagers)
    {
      manager.init(this);
      repositoryManagerMap.put(manager.getType().getName(), manager);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;

  /** Field description */
  private Map<String, GroupManager> groupManagerMap;

  /** Field description */
  private Map<String, RepositoryManager> repositoryManagerMap;
}
