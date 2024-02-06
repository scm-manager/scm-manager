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


@AuditEntry(ignore = true)
@XmlRootElement(name = "group-db")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGroupDatabase implements XmlDatabase<Group>
{
  private Long creationTime;

  @XmlJavaTypeAdapter(XmlGroupMapAdapter.class)
  @XmlElement(name = "groups")
  private Map<String, Group> groupMap = new TreeMap<>();

  private Long lastModified;

  public XmlGroupDatabase()
  {
    long c = System.currentTimeMillis();

    creationTime = c;
    lastModified = c;
  }



  @Override
  public void add(Group group)
  {
    groupMap.put(group.getName(), group);
  }


  @Override
  public boolean contains(String groupname)
  {
    return groupMap.containsKey(groupname);
  }


  @Override
  public Group remove(String groupname)
  {
    return groupMap.remove(groupname);
  }

  
  @Override
  public Collection<Group> values()
  {
    return groupMap.values();
  }



  @Override
  public Group get(String groupname)
  {
    return groupMap.get(groupname);
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
