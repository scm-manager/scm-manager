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



package sonia.scm.user.xml;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.user.User;
import sonia.scm.xml.XmlTimestampDateAdapter;

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
@XmlRootElement(name = "user-db")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlUserDatabase
{

  /**
   * Constructs ...
   *
   */
  public XmlUserDatabase()
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
   * @param user
   */
  public void add(User user)
  {
    userMap.put(user.getName(), user);
  }

  /**
   * Method description
   *
   *
   * @param username
   *
   * @return
   */
  public boolean contains(String username)
  {
    return userMap.containsKey(username);
  }

  /**
   * Method description
   *
   *
   * @param username
   *
   * @return
   */
  public User remove(String username)
  {
    return userMap.remove(username);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<User> values()
  {
    return userMap.values();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param username
   *
   * @return
   */
  public User get(String username)
  {
    return userMap.get(username);
  }

  /**
   * Method description
   *
   *
   * @return
   */
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
  public void setLastModified(long lastModified)
  {
    this.lastModified = lastModified;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlJavaTypeAdapter(XmlTimestampDateAdapter.class)
  private Long creationTime;

  /** Field description */
  @XmlJavaTypeAdapter(XmlTimestampDateAdapter.class)
  private Long lastModified;

  /** Field description */
  @XmlJavaTypeAdapter(XmlUserMapAdapter.class)
  @XmlElement(name = "users")
  private Map<String, User> userMap = new LinkedHashMap<String, User>();
}
