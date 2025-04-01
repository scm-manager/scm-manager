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
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.xml.XmlDatabase;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@AuditEntry(ignore = true)
@XmlRootElement(name = "user-db")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRepositoryRoleDatabase implements XmlDatabase<RepositoryRole> {

  private Long creationTime;
  private Long lastModified;

  @XmlJavaTypeAdapter(XmlRepositoryRoleMapAdapter.class)
  @XmlElement(name = "roles")
  private Map<String, RepositoryRole> roleMap = new TreeMap<>();

  public XmlRepositoryRoleDatabase() {
    long c = System.currentTimeMillis();

    creationTime = c;
    lastModified = c;
  }

  @Override
  public void add(RepositoryRole role) {
    roleMap.put(role.getName(), role);
  }

  @Override
  public boolean contains(String name) {
    return roleMap.containsKey(name);
  }

  @Override
  public RepositoryRole remove(String name) {
    return roleMap.remove(name);
  }

  @Override
  public Collection<RepositoryRole> values() {
    return roleMap.values();
  }

  @Override
  public RepositoryRole get(String name) {
    return roleMap.get(name);
  }

  @Override
  public long getCreationTime() {
    return creationTime;
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  @Override
  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  @Override
  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }
}
