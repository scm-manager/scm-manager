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

package sonia.scm.repository;

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.ModelObject;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.auditlog.AuditLogEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

/**
 * Custom role with specific permissions related to {@link Repository}.
 * This object should be immutable, but could not be due to mapstruct.
 */
@StaticPermissions(value = "repositoryRole", permissions = {}, globalPermissions = {"write"})
@XmlRootElement(name = "roles")
@XmlAccessorType(XmlAccessType.FIELD)
@AuditEntry(labels = "role", ignoredFields = "lastModified")
public class RepositoryRole implements ModelObject, PermissionObject, AuditLogEntity {

  private static final long serialVersionUID = -723588336073192740L;

  private static final String REPOSITORY_MODIFIED_EXCEPTION_TEXT = "roles must not be modified";

  private String name;
  @XmlElement(name = "verb")
  private Set<String> verbs;

  private Long creationDate;
  private Long lastModified;
  private String type;

  /**
   * This constructor exists for mapstruct and JAXB, only -- <b>do not use this in "normal" code</b>.
   *
   * @deprecated Do not use this for "normal" code.
   * Use {@link RepositoryRole#RepositoryRole(String, Collection, String)} instead.
   */
  @Deprecated
  public RepositoryRole() {}

  public RepositoryRole(String name, Collection<String> verbs, String type) {
    this.name = name;
    this.verbs = new LinkedHashSet<>(verbs);
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final RepositoryRole other = (RepositoryRole) obj;

    return Objects.equal(name, other.name)
      && verbs.size() == other.verbs.size()
      && verbs.containsAll(other.verbs);
  }

  /**
   * Returns the hash code value for the {@link RepositoryRole}.
   */
  @Override
  public int hashCode()
  {
    // Normally we do not have a log of repository permissions having the same size of verbs, but different content.
    // Therefore we do not use the verbs themselves for the hash code but only the number of verbs.
    return Objects.hashCode(name, verbs == null? -1: verbs.size());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("verbs", verbs)
            .toString();
  }

  public String getName() {
    return name;
  }

  /**
   * Returns the verb of the role.
   */
  public Collection<String> getVerbs() {
    return verbs == null ? emptyList() : Collections.unmodifiableSet(verbs);
  }

  @Override
  public String getId() {
    return name;
  }

  @Override
  public void setLastModified(Long timestamp) {
    this.lastModified = timestamp;
  }

  @Override
  public Long getCreationDate() {
    return creationDate;
  }

  @Override
  public void setCreationDate(Long timestamp) {
    this.creationDate = timestamp;
  }

  @Override
  public Long getLastModified() {
    return lastModified;
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    if (this.type != null) {
      throw new IllegalStateException(REPOSITORY_MODIFIED_EXCEPTION_TEXT);
    }
    this.type = type;
  }

  @Override
  public boolean isValid() {
    return !Strings.isNullOrEmpty(name) && !verbs.isEmpty();
  }

  /**
   * Use this for creation only. This will throw an {@link IllegalStateException} when modified.
   * @throws IllegalStateException when modified after the value has been set once.
   *
   * @deprecated Do not use this for "normal" code.
   * Use {@link RepositoryRole#RepositoryRole(String, Collection, String)} instead.
   */
  @Deprecated
  public void setName(String name) {
    if (this.name != null) {
      throw new IllegalStateException(REPOSITORY_MODIFIED_EXCEPTION_TEXT);
    }
    this.name = name;
  }

  /**
   * Use this for creation only. This will throw an {@link IllegalStateException} when modified.
   * @throws IllegalStateException when modified after the value has been set once.
   *
   * @deprecated Do not use this for "normal" code.
   * Use {@link RepositoryRole#RepositoryRole(String, Collection, String)} instead.
   */
  @Deprecated
  public void setVerbs(Collection<String> verbs) {
    if (this.verbs != null) {
      throw new IllegalStateException(REPOSITORY_MODIFIED_EXCEPTION_TEXT);
    }
    this.verbs = verbs == null? emptySet(): unmodifiableSet(new LinkedHashSet<>(verbs));
  }

  @Override
  public RepositoryRole clone() {
    try {
      return (RepositoryRole) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String getEntityName() {
    return getId();
  }
}
