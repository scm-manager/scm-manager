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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import sonia.scm.auditlog.AuditEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableCollection;

@StaticPermissions(
  value = "namespace",
  permissions = {"permissionRead", "permissionWrite"},
  custom = true, customGlobal = true
)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "namespaces")
@AuditEntry(labels = "namespace")
public class Namespace implements PermissionObject, Cloneable, RepositoryPermissionHolder {

  private String namespace;
  private Set<RepositoryPermission> permissions = new HashSet<>();

  public Namespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * Constructor for JaxB, only.
   */
  Namespace() {
  }

  public String getNamespace() {
    return namespace;
  }

  public Collection<RepositoryPermission> getPermissions() {
    return unmodifiableCollection(permissions);
  }

  public void setPermissions(Collection<RepositoryPermission> permissions) {
    this.permissions.clear();
    this.permissions.addAll(permissions);
  }

  public void addPermission(RepositoryPermission newPermission) {
    this.permissions.add(newPermission);
  }

  public boolean removePermission(RepositoryPermission permission) {
    return this.permissions.remove(permission);
  }

  @Override
  public String getId() {
    return getNamespace();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (!(o instanceof Namespace)) return false;

    Namespace namespace1 = (Namespace) o;

    return new EqualsBuilder()
      .append(namespace, namespace1.namespace)
      .append(permissions, namespace1.permissions)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(namespace)
      .append(permissions)
      .toHashCode();
  }

  @Override
  public String toString() {
    return "Namespace{" +
      "namespace='" + namespace + '\'' +
      ", permissions=" + permissions +
      '}';
  }

  @Override
  public Namespace clone() {
    try {
      Namespace clone = (Namespace) super.clone();
      clone.permissions = new HashSet<>(permissions);
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }
  }
}
