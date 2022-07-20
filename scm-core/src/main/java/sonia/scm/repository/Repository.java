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

import com.github.sdorra.ssp.Guard;
import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.BasicPropertiesAware;
import sonia.scm.ModelObject;
import sonia.scm.search.Indexed;
import sonia.scm.search.IndexedType;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Source code repository.
 *
 * @author Sebastian Sdorra
 */
@IndexedType
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repositories")
@StaticPermissions(
  value = "repository",
  permissions = {"read", "modify", "delete", "rename", "healthCheck", "pull", "push", "permissionRead", "permissionWrite", "archive", "export"},
  custom = true, customGlobal = true,
  guards = {
    @Guard(guard = RepositoryPermissionGuard.class)
  }
)
public class Repository extends BasicPropertiesAware implements ModelObject, PermissionObject, RepositoryCoordinates {

  private static final long serialVersionUID = 3486560714961909711L;

  private String id;

  @Indexed(defaultQuery = true, boost = 1.25f, analyzer = Indexed.Analyzer.IDENTIFIER)
  private String namespace;

  @Indexed(defaultQuery = true, boost = 1.5f, analyzer = Indexed.Analyzer.IDENTIFIER)
  private String name;
  @Indexed(type = Indexed.Type.SEARCHABLE)
  private String type;
  @Indexed(defaultQuery = true, highlighted = true)
  private String description;
  private String contact;
  @Indexed
  private Long creationDate;
  @Indexed
  private Long lastModified;
  @XmlTransient
  private List<HealthCheckFailure> healthCheckFailures;
  @XmlElement(name = "permission")
  private Set<RepositoryPermission> permissions = new HashSet<>();
  private boolean archived;

  /**
   * Constructs a new {@link Repository}.
   * This constructor is used by JAXB.
   */
  public Repository() {
  }

  /**
   * Constructs a new {@link Repository}.
   *
   * @param id   id of the {@link Repository}
   * @param type type of the {@link Repository}
   * @param name name of the {@link Repository}
   */
  public Repository(String id, String type, String namespace, String name) {
    this.id = id;
    this.type = type;
    this.namespace = namespace;
    this.name = name;
  }

  /**
   * Constructs a new {@link Repository}.
   *
   * @param id          id of the {@link Repository}
   * @param type        type of the {@link Repository}
   * @param name        name of the {@link Repository}
   * @param namespace   namespace of the {@link Repository}
   * @param contact     email address of a person who is responsible for
   *                    this repository.
   * @param description a short description of the repository
   * @param permissions permissions for specific users and groups.
   */
  public Repository(String id, String type, String namespace, String name, String contact,
                    String description, RepositoryPermission... permissions) {
    this.id = id;
    this.type = type;
    this.namespace = namespace;
    this.name = name;
    this.contact = contact;
    this.description = description;

    if (Util.isNotEmpty(permissions)) {
      this.permissions.addAll(Arrays.asList(permissions));
    }
  }

  /**
   * Returns a contact email address of a person who is responsible for
   * the {@link Repository}.
   *
   * @return contact email address
   */
  public String getContact() {
    return contact;
  }

  /**
   * Returns a timestamp of the creation date of the {@link Repository}.
   *
   * @return a timestamp of the creation date of the {@link Repository}
   */
  public Long getCreationDate() {
    return creationDate;
  }

  /**
   * Returns a short description of the {@link Repository}.
   *
   * @return short description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns a {@link List} of {@link HealthCheckFailure}s. The {@link List}
   * is empty if the repository is healthy.
   *
   * @return {@link List} of {@link HealthCheckFailure}s
   * @since 1.36
   */
  public List<HealthCheckFailure> getHealthCheckFailures() {
    if (healthCheckFailures == null) {
      healthCheckFailures = Collections.emptyList();
    }

    return healthCheckFailures;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Long getLastModified() {
    return lastModified;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @XmlTransient
  public NamespaceAndName getNamespaceAndName() {
    return new NamespaceAndName(getNamespace(), getName());
  }

  public Collection<RepositoryPermission> getPermissions() {
    return Collections.unmodifiableCollection(permissions);
  }

  /**
   * Returns the permission for the given user, if present, or an empty {@link Optional} otherwise.
   *
   * @since 2.38.0
   */
  public Optional<RepositoryPermission> findUserPermission(String userId) {
    return findPermission(userId, false);
  }

  /**
   * Returns the permission for the given group, if present, or an empty {@link Optional} otherwise.
   *
   * @since 2.38.0
   */
  public Optional<RepositoryPermission> findGroupPermission(String groupId) {
    return findPermission(groupId, true);
  }

  private Optional<RepositoryPermission> findPermission(String x, boolean isGroup) {
    return getPermissions().stream().filter(p -> p.isGroupPermission() == isGroup && p.getName().equals(x)).findFirst();
  }

  /**
   * Returns the type (hg, git, svn ...) of the {@link Repository}.
   *
   * @return type of the repository
   */
  @Override
  public String getType() {
    return type;
  }

  /**
   * Returns <code>true</code>, when the repository is marked as "archived". An archived repository cannot be modified.
   *
   * @since 2.11.0
   */
  public boolean isArchived() {
    return archived;
  }

  /**
   * Returns {@code true} if the repository is healthy.
   *
   * @return {@code true} if the repository is healthy
   * @since 1.36
   */
  public boolean isHealthy() {
    return Util.isEmpty(healthCheckFailures);
  }

  /**
   * Returns true if the {@link Repository} is valid.
   * <ul>
   * <li>The namespace is valid</li>
   * <li>The name is valid</li>
   * <li>The type is not empty</li>
   * <li>The contact is empty or contains a valid email address</li>
   * </ul>
   *
   * @return true if the {@link Repository} is valid
   */
  @Override
  public boolean isValid() {
    return ValidationUtil.isNameValid(namespace)
      && ValidationUtil.isRepositoryNameValid(name)
      && Util.isNotEmpty(type)
      && ((Util.isEmpty(contact)) || ValidationUtil.isMailAddressValid(contact));
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public void setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setName(String name) {
    this.name = name;
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

  public void setType(String type) {
    this.type = type;
  }

  /**
   * Set this to <code>true</code> to mark the repository as "archived". An archived repository cannot be modified.
   *
   * @since 2.11.0
   */
  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  public void setHealthCheckFailures(List<HealthCheckFailure> healthCheckFailures) {
    this.healthCheckFailures = healthCheckFailures;
  }

  @Override
  public Repository clone() {
    Repository repository = null;

    try {
      repository = (Repository) super.clone();
      // fix permission reference on clone
      repository.permissions = new HashSet<>(permissions);
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }

    return repository;
  }

  /**
   * Copies all properties of the {@link Repository} to the given one.
   *
   * @param repository the target {@link Repository}
   */
  public void copyProperties(Repository repository) {
    repository.setNamespace(namespace);
    repository.setName(name);
    repository.setContact(contact);
    repository.setCreationDate(creationDate);
    repository.setLastModified(lastModified);
    repository.setDescription(description);
    repository.setPermissions(permissions);

    // do not copy health check results
  }

  /**
   * Returns true if the {@link Repository} is the same as the obj argument.
   *
   * @param obj the reference object with which to compare
   * @return true if the {@link Repository} is the same as the obj argument
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Repository other = (Repository) obj;

    return Objects.equal(id, other.id)
      && Objects.equal(namespace, other.namespace)
      && Objects.equal(name, other.name)
      && Objects.equal(contact, other.contact)
      && Objects.equal(description, other.description)
      && Objects.equal(permissions, other.permissions)
      && Objects.equal(type, other.type)
      && Objects.equal(creationDate, other.creationDate)
      && Objects.equal(lastModified, other.lastModified)
      && Objects.equal(properties, other.properties)
      && Objects.equal(healthCheckFailures, other.healthCheckFailures);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, namespace, name, contact, description,
      permissions, type, creationDate, lastModified, properties,
      healthCheckFailures);
  }

  @Override
  public String toString() {
    String idString = id == null ? "no-id" : id;
    if (name == null) {
      return "unnamed repository (" + idString + ")";
    } else if (namespace == null) {
      return "no-namespace/" + name + " (" + idString + ")";
    }
    return namespace + "/" + name + " (" + id + ")";
  }

  public String toFullString() {
    return MoreObjects.toStringHelper(this)
      .add("id", id)
      .add("namespace", namespace)
      .add("name", name)
      .add("contact", contact)
      .add("description", description)
      .add("permissions", permissions)
      .add("type", type)
      .add("lastModified", lastModified)
      .add("creationDate", creationDate)
      .add("properties", properties)
      .add("healthCheckFailures", healthCheckFailures)
      .toString();
  }
}
