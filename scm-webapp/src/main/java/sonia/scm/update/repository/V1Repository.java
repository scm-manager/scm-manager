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

package sonia.scm.update.repository;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.update.V1Properties;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repositories")
public class V1Repository {
  private String contact;
  private long creationDate;
  private Long lastModified;
  private String description;
  private String id;
  private String name;
  @XmlElement(name="public")
  private boolean isPublic;
  private boolean archived;
  private String type;
  private List<V1Permission> permissions;
  private V1Properties properties;

  public V1Repository() {
  }

  public V1Repository(String id, String type, String name) {
    this.id = id;
    this.type = type;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getContact() {
    return contact;
  }

  public long getCreationDate() {
    return creationDate;
  }

  public Long getLastModified() {
    return lastModified;
  }

  public String getDescription() {
    return description;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public boolean isArchived() {
    return archived;
  }

  public List<V1Permission> getPermissions() {
    return permissions;
  }

  public V1Properties getProperties() {
    return properties;
  }

  @Override
  public String toString() {
    return "V1Repository{" +
      ", contact='" + contact + '\'' +
      ", creationDate=" + creationDate +
      ", lastModified=" + lastModified +
      ", description='" + description + '\'' +
      ", id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", isPublic=" + isPublic +
      ", archived=" + archived +
      ", type='" + type + '\'' +
      '}';
  }
}
