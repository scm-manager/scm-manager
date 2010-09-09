/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.group;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "groups")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "type", "name", "members" })
public class Group implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = 1752369869345245872L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public Group() {}

  /**
   * Constructs ...
   *
   *
   *
   * @param type
   * @param name
   */
  public Group(String type, String name)
  {
    this.type = type;
    this.name = name;
    this.members = new ArrayList<String>();
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param type
   * @param name
   * @param members
   */
  public Group(String type, String name, List<String> members)
  {
    this.type = type;
    this.name = name;
    this.members = members;
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param type
   * @param name
   * @param members
   */
  public Group(String type, String name, String... members)
  {
    this.type = type;
    this.name = name;
    this.members = new ArrayList<String>();

    if (Util.isNotEmpty(members))
    {
      this.members.addAll(Arrays.asList(members));
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param member
   *
   * @return
   */
  public boolean add(String member)
  {
    return members.add(member);
  }

  /**
   * Method description
   *
   */
  public void clear()
  {
    members.clear();
  }

  /**
   * Method description
   *
   *
   * @param member
   *
   * @return
   */
  public boolean remove(String member)
  {
    return members.remove(member);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    StringBuilder msg = new StringBuilder();

    msg.append(name).append(" [");

    if (Util.isNotEmpty(members))
    {
      Iterator<String> it = members.iterator();

      while (it.hasNext())
      {
        msg.append(it.next());

        if (it.hasNext())
        {
          msg.append(",");
        }
      }
    }

    return msg.append("]").toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<String> getMembers()
  {
    return members;
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
   * @param members
   */
  public void setMembers(List<String> members)
  {
    this.members = members;
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
  private List<String> members;

  /** Field description */
  private String name;

  /** Field description */
  private String type;
}
