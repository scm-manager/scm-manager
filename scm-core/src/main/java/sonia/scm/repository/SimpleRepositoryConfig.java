/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public class SimpleRepositoryConfig extends BasicRepositoryConfig
{

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
   * @param repositoryDirectory
   */
  public void setRepositoryDirectory(File repositoryDirectory)
  {
    this.repositoryDirectory = repositoryDirectory;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File repositoryDirectory;
}
