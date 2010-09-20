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
public class HgConfig extends BasicRepositoryConfig
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
  public String getHgBinary()
  {
    return hgBinary;
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
   * @param hgBinary
   */
  public void setHgBinary(String hgBinary)
  {
    this.hgBinary = hgBinary;
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
  private String hgBinary = "hg";

  /** Field description */
  private File repositoryDirectory;
}
