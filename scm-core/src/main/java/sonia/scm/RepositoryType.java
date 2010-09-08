/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryType
{

  /**
   * Constructs ...
   *
   */
  public RepositoryType() {}

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param displayName
   */
  public RepositoryType(String name, String displayName)
  {
    this.name = name;
    this.displayName = displayName;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param displayName
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String displayName;

  /** Field description */
  private String name;
}
