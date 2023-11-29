/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.group.xml;

//~--- non-JDK imports --------------------------------------------------------

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.group.Group;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "groups")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGroupList implements Iterable<Group>
{

  /**
   * Constructs ...
   *
   */
  public XmlGroupList() {}

  /**
   * Constructs ...
   *
   *
   *
   * @param groupMap
   */
  public XmlGroupList(Map<String, Group> groupMap)
  {
    this.groups = new LinkedList<>(groupMap.values());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Iterator<Group> iterator()
  {
    return groups.iterator();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public LinkedList<Group> getGroups()
  {
    return groups;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param groups
   */
  public void setGroups(LinkedList<Group> groups)
  {
    this.groups = groups;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "group")
  private LinkedList<Group> groups;
}
