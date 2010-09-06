/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "state")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmState
{

  /**
   * Constructs ...
   *
   */
  public ScmState() {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public RepositoryType[] getRepositoryTypes()
  {
    return repositoryTypes;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSuccess()
  {
    return success;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repositoryTypes
   */
  public void setRepositoryTypes(RepositoryType[] repositoryTypes)
  {
    this.repositoryTypes = repositoryTypes;
  }

  /**
   * Method description
   *
   *
   * @param success
   */
  public void setSuccess(boolean success)
  {
    this.success = success;
  }

  /**
   * Method description
   *
   *
   * @param username
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "repositoryTypes")
  private RepositoryType[] repositoryTypes;

  /** Field description */
  private boolean success = true;

  /** Field description */
  private String username;
}
