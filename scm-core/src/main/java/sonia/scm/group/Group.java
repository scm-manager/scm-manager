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

package sonia.scm.group;


import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.BasicPropertiesAware;
import sonia.scm.ModelObject;
import sonia.scm.ReducedModelObject;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.auditlog.AuditLogEntity;
import sonia.scm.search.Indexed;
import sonia.scm.search.IndexedType;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Organizes users into a group for easier permissions management.
 * <p>
 * TODO for 2.0: Use a set instead of a list for members
 *
 */
@IndexedType(permission = "group:list")
@StaticPermissions(
  value = "group",
  globalPermissions = {"create", "list", "autocomplete"},
  custom = true, customGlobal = true
)
@XmlRootElement(name = "groups")
@XmlAccessorType(XmlAccessType.FIELD)
@AuditEntry(labels = "group", ignoredFields = "lastModified")
public class Group extends BasicPropertiesAware
  implements ModelObject, PermissionObject, ReducedModelObject, AuditLogEntity {

  private static final long serialVersionUID = 1752369869345245872L;

  private boolean external = false;

  @Indexed
  private Long creationDate;

  @Indexed(defaultQuery = true, highlighted = true)
  private String description;

  @Indexed
  private Long lastModified;

  private List<String> members;

  @Indexed(defaultQuery = true, boost = 1.5f)
  private String name;

  private String type;



  /**
   * This constructor is required by JAXB.
   */
  public Group() {
  }

  public Group(String type, String name) {
    this.type = type;
    this.name = name;
    this.members = Lists.newArrayList();
  }

  public Group(String type, String name, List<String> members) {
    this.type = type;
    this.name = name;
    this.members = members;
  }

  public Group(String type, String name, String... members) {
    this.type = type;
    this.name = name;
    this.members = Lists.newArrayList();

    if (Util.isNotEmpty(members)) {
      this.members.addAll(Arrays.asList(members));
    }
  }

  public boolean add(String member) {
    return getMembers().add(member);
  }

  public void clear() {
    members.clear();
  }

  @Override
  public Group clone() {
    Group group = null;

    try {
      group = (Group) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }

    return group;
  }

  /**
   * Copies all properties of this group to the given one.
   *
   * @param group to copies all properties of this one
   */
  public void copyProperties(Group group) {
    group.setName(name);
    group.setMembers(members);
    group.setType(type);
    group.setDescription(description);
    group.setExternal(external);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Group other = (Group) obj;

    return Objects.equal(name, other.name)
      && Objects.equal(description, other.description)
      && Objects.equal(members, other.members)
      && Objects.equal(type, other.type)
      && Objects.equal(external, other.external)
      && Objects.equal(creationDate, other.creationDate)
      && Objects.equal(lastModified, other.lastModified)
      && Objects.equal(properties, other.properties);
  }

  /**
   * Returns a hash code value for this {@link Group}.
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(name, description, members, type, creationDate,
      lastModified, properties);
  }

  public boolean remove(String member) {
    return members.remove(member);
  }

  @Override
  public String toString() {
    //J-
    return MoreObjects.toStringHelper(this)
      .add("name", name)
      .add("description", description)
      .add("members", members)
      .add("type", type)
      .add("external", external)
      .add("creationDate", creationDate)
      .add("lastModified", lastModified)
      .add("properties", properties)
      .toString();
    //J+
  }


  public Long getCreationDate() {
    return creationDate;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Returns the unique name of this group. This method is an alias for the
   * {@link #getName()} method.
   */
  @Override
  public String getId() {
    return name;
  }

  @Override
  public String getDisplayName() {
    return description;
  }

  @Override
  public Long getLastModified() {
    return lastModified;
  }

  public List<String> getMembers() {
    if (external) {
      return Collections.emptyList();
    } else if (members == null) {
      members = Lists.newArrayList();
    }

    return members;
  }

  /**
   * Returns the unique name of this group.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of this group. The default type is xml.
   */
  @Override
  public String getType() {
    return type;
  }

  public boolean isExternal() {
    return external;
  }

  public boolean isMember(String member) {
    return (members != null) && members.contains(member);
  }

  @Override
  public boolean isValid() {
    return ValidationUtil.isNameValid(name) && Util.isNotEmpty(type);
  }


  public void setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setExternal(boolean external) {
    this.external = external;
  }

  /**
   * Get the entity name which is used for the audit log
   * @since 2.43.0
   */
  @Override
  public String getEntityName() {
    return getName();
  }
}
