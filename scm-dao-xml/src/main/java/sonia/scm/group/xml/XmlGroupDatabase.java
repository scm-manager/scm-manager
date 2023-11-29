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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.group.Group;
import sonia.scm.xml.XmlDatabase;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Sebastian Sdorra
 */
@AuditEntry(ignore = true)
@XmlRootElement(name = "group-db")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGroupDatabase implements XmlDatabase<Group>
{

  /**
   * Constructs ...
   *
   */
  public XmlGroupDatabase()
  {
    long c = System.currentTimeMillis();

    creationTime = c;
    lastModified = c;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param group
   */
  @Override
  public void add(Group group)
  {
    groupMap.put(group.getName(), group);
  }

  /**
   * Method description
   *
   *
   * @param groupname
   *
   * @return
   */
  @Override
  public boolean contains(String groupname)
  {
    return groupMap.containsKey(groupname);
  }

  /**
   * Method description
   *
   *
   * @param groupname
   *
   * @return
   */
  @Override
  public Group remove(String groupname)
  {
    return groupMap.remove(groupname);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Group> values()
  {
    return groupMap.values();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param groupname
   *
   * @return
   */
  @Override
  public Group get(String groupname)
  {
    return groupMap.get(groupname);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public long getCreationTime()
  {
    return creationTime;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public long getLastModified()
  {
    return lastModified;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param creationTime
   */
  @Override
  public void setCreationTime(long creationTime)
  {
    this.creationTime = creationTime;
  }

  /**
   * Method description
   *
   *
   * @param lastModified
   */
  @Override
  public void setLastModified(long lastModified)
  {
    this.lastModified = lastModified;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Long creationTime;

  /** Field description */
  @XmlJavaTypeAdapter(XmlGroupMapAdapter.class)
  @XmlElement(name = "groups")
  private Map<String, Group> groupMap = new TreeMap<>();

  /** Field description */
  private Long lastModified;
}
