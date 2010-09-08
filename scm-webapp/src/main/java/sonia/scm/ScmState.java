/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import sonia.scm.repository.RepositoryType;
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

  /**
   * Constructs ...
   *
   *
   * @param user
   * @param repositoryTypes
   */
  public ScmState(User user, RepositoryType[] repositoryTypes)
  {
    this.user = user;
    this.repositoryTypes = repositoryTypes;
  }

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
  public User getUser()
  {
    return user;
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
   * @param user
   */
  public void setUser(User user)
  {
    this.user = user;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "repositoryTypes")
  private RepositoryType[] repositoryTypes;

  /** Field description */
  private boolean success = true;

  /** Field description */
  private User user;
}
