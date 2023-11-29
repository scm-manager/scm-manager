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
  globalPermissions = {"permissionRead", "permissionWrite"},
  permissions = {},
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
