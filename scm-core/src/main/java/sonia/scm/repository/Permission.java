/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name="permissions")
@XmlAccessorType(XmlAccessType.FIELD)
public class Permission
{

  /**
   * Constructs ...
   *
   */
  public Permission() {}

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param readable
   * @param writeable
   */
  public Permission(String name, boolean readable, boolean writeable)
  {
    this.name = name;
    this.readable = readable;
    this.writeable = writeable;
    this.groupPermission = false;
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param readable
   * @param writeable
   * @param groupPermission
   */
  public Permission(String name, boolean readable, boolean writeable,
                    boolean groupPermission)
  {
    this.name = name;
    this.readable = readable;
    this.writeable = writeable;
    this.groupPermission = groupPermission;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();

    buffer.append(name);

    if (groupPermission)
    {
      buffer.append(" (Group)");
    }

    buffer.append(" - ");

    if (readable)
    {
      buffer.append("r");
    }

    if (writeable)
    {
      buffer.append("w");
    }

    return buffer.toString();
  }

  //~--- get methods ----------------------------------------------------------

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

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isGroupPermission()
  {
    return groupPermission;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isReadable()
  {
    return readable;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isWriteable()
  {
    return writeable;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean groupPermission;

  /** Field description */
  private String name;

  /** Field description */
  private boolean readable;

  /** Field description */
  private boolean writeable;
}
