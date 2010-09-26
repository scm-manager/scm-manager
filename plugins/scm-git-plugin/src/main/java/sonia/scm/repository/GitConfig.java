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
public class GitConfig extends SimpleRepositoryConfig
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getGitBinary()
  {
    return gitBinary;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param gitBinary
   */
  public void setGitBinary(String gitBinary)
  {
    this.gitBinary = gitBinary;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String gitBinary = "git";
}
