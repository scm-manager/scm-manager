/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "config")
public class HgConfig
{

  /**
   * Constructs ...
   *
   */
  public HgConfig() {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public File getConfigDirectory()
  {
    return configDirectory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public File getRepositoryDirectory()
  {
    return repositoryDirectory;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param configDirectory
   */
  public void setConfigDirectory(File configDirectory)
  {
    this.configDirectory = configDirectory;
  }

  /**
   * Method description
   *
   *
   * @param repositoryDirectory
   */
  public void setRepositoryDirectory(File repositoryDirectory)
  {
    this.repositoryDirectory = repositoryDirectory;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File configDirectory;

  /** Field description */
  private File repositoryDirectory;
}
