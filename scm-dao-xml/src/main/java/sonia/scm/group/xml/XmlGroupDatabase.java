/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
