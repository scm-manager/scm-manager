/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.SCMContextProvider;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Date;
import java.util.UUID;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <C>
 */
public abstract class AbstractRepositoryHandler<C> implements RepositoryHandler
{

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  protected abstract String buildUrl(Repository repository);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract Class<C> getConfigClass();

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

    // do nothing
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
    String name = getType().getName();

    configFile =
      new File(context.getBaseDirectory(),
               "config".concat(File.separator).concat(name).concat(".xml"));
    loadConfig();
  }

  /**
   * Method description
   *
   */
  public void loadConfig()
  {
    if (configFile.exists())
    {
      config = JAXB.unmarshal(configFile, getConfigClass());
    }
  }

  /**
   * Method description
   *
   */
  public void storeConfig()
  {
    if (config != null)
    {
      JAXB.marshal(config, configFile);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public C getConfig()
  {
    return config;
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

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param config
   */
  public void setConfig(C config)
  {
    this.config = config;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   */
  protected void initNewRepository(Repository repository)
  {
    repository.setId(UUID.randomUUID().toString());
    repository.setUrl(buildUrl(repository));
    repository.setCreationDate(new Date());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected C config;

  /** Field description */
  protected File configFile;
}
