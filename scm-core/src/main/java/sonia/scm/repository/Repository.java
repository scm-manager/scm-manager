/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "repositories")
@XmlType(propOrder =
{
  "id", "type", "name", "contact", "description", "permissions"
})
public class Repository implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = 3486560714961909711L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public Repository() {}

  /**
   * Constructs ...
   *
   *
   *
   * @param id
   * @param type
   * @param name
   */
  public Repository(String id, String type, String name)
  {
    this.id = id;
    this.type = type;
    this.name = name;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param id
   * @param type
   * @param name
   * @param contact
   * @param description
   * @param permissions
   */
  public Repository(String id, String type, String name, String contact,
                    String description, Permission... permissions)
  {
    this.id = id;
    this.type = type;
    this.name = name;
    this.contact = contact;
    this.description = description;
    this.permissions = new ArrayList<Permission>();

    if (Util.isNotEmpty(permissions))
    {
      this.permissions.addAll(Arrays.asList(permissions));
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getContact()
  {
    return contact;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return id;
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

  /**
   * Method description
   *
   *
   * @return
   */
  public List<Permission> getPermissions()
  {
    return permissions;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getType()
  {
    return type;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param contact
   */
  public void setContact(String contact)
  {
    this.contact = contact;
  }

  /**
   * Method description
   *
   *
   * @param description
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  public void setId(String id)
  {
    this.id = id;
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

  /**
   * Method description
   *
   *
   * @param permissions
   */
  public void setPermissions(List<Permission> permissions)
  {
    this.permissions = permissions;
  }

  /**
   * Method description
   *
   *
   * @param type
   */
  public void setType(String type)
  {
    this.type = type;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String contact;

  /** Field description */
  private String description;

  /** Field description */
  private String id;

  /** Field description */
  private String name;

  /** Field description */
  private List<Permission> permissions;

  /** Field description */
  private String type;
}
