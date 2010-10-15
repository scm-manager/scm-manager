/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "config")
public class SvnConfig extends SimpleRepositoryConfig
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getSvnAccessFile()
  {
    return svnAccessFile;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getSvnAdminBinary()
  {
    return svnAdminBinary;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param svnAccessFile
   */
  public void setSvnAccessFile(String svnAccessFile)
  {
    this.svnAccessFile = svnAccessFile;
  }

  /**
   * Method description
   *
   *
   * @param svnAdminBinary
   */
  public void setSvnAdminBinary(String svnAdminBinary)
  {
    this.svnAdminBinary = svnAdminBinary;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String svnAccessFile;

  /** Field description */
  private String svnAdminBinary;
}
