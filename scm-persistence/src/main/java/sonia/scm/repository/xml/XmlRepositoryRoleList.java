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

package sonia.scm.repository.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.repository.RepositoryRole;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

@XmlRootElement(name = "roles")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRepositoryRoleList implements Iterable<RepositoryRole> {

  public XmlRepositoryRoleList() {}

  public XmlRepositoryRoleList(Map<String, RepositoryRole> roleMap) {
    this.roles = new LinkedList<RepositoryRole>(roleMap.values());
  }

  @Override
  public Iterator<RepositoryRole> iterator()
  {
    return roles.iterator();
  }

  public LinkedList<RepositoryRole> getRoles()
  {
    return roles;
  }

  public void setRoles(LinkedList<RepositoryRole> roles)
  {
    this.roles = roles;
  }

  @XmlElement(name = "role")
  private LinkedList<RepositoryRole> roles;
}
