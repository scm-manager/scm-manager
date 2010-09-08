/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "repositories")
@XmlType(propOrder =
{
  "type", "name", "contact", "description"
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
   * @param type
   * @param name
   */
  public Repository(String type, String name)
  {
    this.type = type;
    this.name = name;
  }

  /**
   * Constructs ...
   *
   *
   * @param type
   * @param name
   * @param contact
   * @param description
   */
  public Repository(String type, String name, String contact,
                    String description)
  {
    this.type = type;
    this.name = name;
    this.contact = contact;
    this.description = description;
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
  private String name;

  /** Field description */
  private String type;
}
