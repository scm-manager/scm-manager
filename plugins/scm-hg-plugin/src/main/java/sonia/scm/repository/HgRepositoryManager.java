/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.SCMContextProvider;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Collection;

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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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
    throw new UnsupportedOperationException("Not supported yet.");
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgConfig config;
}
