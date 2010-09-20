/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "permissions")
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
   * @param writeable
   */
  public Permission(String name, boolean writeable)
  {
    this.name = name;
    this.writeable = writeable;
    this.groupPermission = false;
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param writeable
   * @param groupPermission
   */
  public Permission(String name, boolean writeable, boolean groupPermission)
  {
    this.name = name;
    this.writeable = writeable;
    this.groupPermission = groupPermission;
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param writeable
   * @param groupPermission
   * @param path
   */
  public Permission(String name, boolean writeable, boolean groupPermission,
                    String path)
  {
    this.name = name;
    this.writeable = writeable;
    this.groupPermission = groupPermission;
    this.path = path;
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

    buffer.append(" - r");

    if (writeable)
    {
      buffer.append("w");
    }

    if (Util.isNotEmpty(path))
    {
      buffer.append(" ").append(path);
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
  public String getPath()
  {
    return path;
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
  public boolean isRootPermission()
  {
    return Util.isEmpty(path);
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

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   */
  public void setPath(String path)
  {
    this.path = path;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean groupPermission;

  /** Field description */
  private String name;

  /** Field description */
  private String path = "";

  /** Field description */
  private boolean writeable;
}
