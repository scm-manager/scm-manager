/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.group.xml;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.group.Group;
import sonia.scm.xml.XmlDatabase;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
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
  private Map<String, Group> groupMap = new LinkedHashMap<String, Group>();

  /** Field description */
  private Long lastModified;
}
