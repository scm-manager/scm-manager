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

package sonia.scm.user.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.user.User;
import sonia.scm.xml.XmlDatabase;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;


@AuditEntry(ignore = true)
@XmlRootElement(name = "user-db")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlUserDatabase implements XmlDatabase<User>
{
  private Long creationTime;

  private Long lastModified;

  @XmlJavaTypeAdapter(XmlUserMapAdapter.class)
  @XmlElement(name = "users")
  private Map<String, User> userMap = new TreeMap<>();

  public XmlUserDatabase()
  {
    long c = System.currentTimeMillis();

    creationTime = c;
    lastModified = c;
  }



  @Override
  public void add(User user)
  {
    userMap.put(user.getName(), user);
  }


  @Override
  public boolean contains(String username)
  {
    return userMap.containsKey(username);
  }


  @Override
  public User remove(String username)
  {
    return userMap.remove(username);
  }

  
  @Override
  public Collection<User> values()
  {
    return userMap.values();
  }



  @Override
  public User get(String username)
  {
    return userMap.get(username);
  }

  
  @Override
  public long getCreationTime()
  {
    return creationTime;
  }

  
  @Override
  public long getLastModified()
  {
    return lastModified;
  }



  @Override
  public void setCreationTime(long creationTime)
  {
    this.creationTime = creationTime;
  }


  @Override
  public void setLastModified(long lastModified)
  {
    this.lastModified = lastModified;
  }

}
