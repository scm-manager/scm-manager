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
import sonia.scm.group.Group;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


@XmlRootElement(name = "groups")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlGroupList implements Iterable<Group>
{
  @XmlElement(name = "group")
  private LinkedList<Group> groups;

  public XmlGroupList() {}


  public XmlGroupList(Map<String, Group> groupMap)
  {
    this.groups = new LinkedList<>(groupMap.values());
  }



  @Override
  public Iterator<Group> iterator()
  {
    return groups.iterator();
  }



  public LinkedList<Group> getGroups()
  {
    return groups;
  }



  public void setGroups(LinkedList<Group> groups)
  {
    this.groups = groups;
  }

}
