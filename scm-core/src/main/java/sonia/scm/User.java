/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.security.Principal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder =
{
  "name", "displayName", "mail", "password"
})
public class User implements Principal, Serializable
{

  /** Field description */
  private static final long serialVersionUID = -3089541936726329663L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public User() {}

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param displayName
   * @param mail
   */
  public User(String name, String displayName, String mail)
  {
    this.name = name;
    this.displayName = displayName;
    this.mail = mail;
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
  public String getMail()
  {
    return mail;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
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
  public String getPassword()
  {
    return password;
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
   * @param mail
   */
  public void setMail(String mail)
  {
    this.mail = mail;
  }

  /**
   * Method description
   *
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
   * @param password
   */
  public void setPassword(String password)
  {
    this.password = password;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String displayName;

  /** Field description */
  private String mail;

  /** Field description */
  private String name;

  /** Field description */
  private String password;
}
