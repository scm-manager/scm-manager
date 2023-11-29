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

//~--- non-JDK imports --------------------------------------------------------

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

//~--- JDK imports ------------------------------------------------------------

/**
 * Organizes users into a group for easier permissions management.
 * <p>
 * TODO for 2.0: Use a set instead of a list for members
 *
 * @author Sebastian Sdorra
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

  /**
   * Field description
   */
  private static final long serialVersionUID = 1752369869345245872L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs {@link Group} object. This constructor is required by JAXB.
   */
  public Group() {
  }

  /**
   * Constructs {@link Group} object.
   *
   * @param type of the group
   * @param name of the group
   */
  public Group(String type, String name) {
    this.type = type;
    this.name = name;
    this.members = Lists.newArrayList();
  }

  /**
   * Constructs {@link Group} object.
   *
   * @param type    of the group
   * @param name    of the group
   * @param members of the groups
   */
  public Group(String type, String name, List<String> members) {
    this.type = type;
    this.name = name;
    this.members = members;
  }

  /**
   * Constructs {@link Group} object.
   *
   * @param type    of the group
   * @param name    of the group
   * @param members of the groups
   */
  public Group(String type, String name, String... members) {
    this.type = type;
    this.name = name;
    this.members = Lists.newArrayList();

    if (Util.isNotEmpty(members)) {
      this.members.addAll(Arrays.asList(members));
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Add a new member to the group.
   *
   * @param member - The name of new group member
   * @return true if the operation was successful
   */
  public boolean add(String member) {
    return getMembers().add(member);
  }

  /**
   * Remove all members of the group.
   */
  public void clear() {
    members.clear();
  }

  /**
   * Returns a clone of the group.
   *
   * @return a clone of the group
   */
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

  /**
   * Returns true if this {@link Group} is the same as the obj argument.
   *
   * @param obj - the reference object with which to compare
   * @return true if this {@link Group} is the same as the obj argument
   */
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
   *
   * @return a hash code value for this {@link Group}
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(name, description, members, type, creationDate,
      lastModified, properties);
  }

  /**
   * Remove the given member from this group.
   *
   * @param member to remove from this group
   * @return true if the operation was successful
   */
  public boolean remove(String member) {
    return members.remove(member);
  }

  /**
   * Returns a {@link String} that represents this group.
   *
   * @return a {@link String} that represents this group
   */
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a timestamp of the creation date of this group.
   *
   * @return a timestamp of the creation date of this group
   */
  public Long getCreationDate() {
    return creationDate;
  }

  /**
   * Returns the description of this group.
   *
   * @return the description of this group
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the unique name of this group. This method is an alias for the
   * {@link #getName()} method.
   *
   * @return the unique name of this group
   */
  @Override
  public String getId() {
    return name;
  }

  @Override
  public String getDisplayName() {
    return description;
  }

  /**
   * Returns a timestamp of the last modified date of this group.
   *
   * @return a timestamp of the last modified date of this group
   */
  @Override
  public Long getLastModified() {
    return lastModified;
  }

  /**
   * Returns a {@link java.util.List} of all members of this group.
   *
   * @return a {@link java.util.List} of all members of this group
   */
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
   *
   * @return the unique name of this group
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of this group. The default type is xml.
   *
   * @return the type of this group
   */
  @Override
  public String getType() {
    return type;
  }

  /**
   * Returns {@code true} if the members of the groups managed external of scm-manager.
   *
   * @return {@code true} if the group is an external group
   */
  public boolean isExternal() {
    return external;
  }

  /**
   * Returns true if the member is a member of this group.
   *
   * @param member - The name of the member
   * @return true if the member is a member of this group
   */
  public boolean isMember(String member) {
    return (members != null) && members.contains(member);
  }

  /**
   * Returns true if the group is valid.
   *
   * @return true if the group is valid
   */
  @Override
  public boolean isValid() {
    return ValidationUtil.isNameValid(name) && Util.isNotEmpty(type);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the date the group was created.
   *
   * @param creationDate - date the group was last modified
   */
  public void setCreationDate(Long creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Sets the description of the group.
   *
   * @param description of the group
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the date the group was last modified.
   *
   * @param lastModified - date the group was last modified
   */
  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Sets the members of the group.
   *
   * @param members of the group
   */
  public void setMembers(List<String> members) {
    this.members = members;
  }

  /**
   * Sets the name of the group.
   *
   * @param name of the group
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the type of the group.
   *
   * @param type of the group
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * {@code true} to mark the group as external.
   *
   * @param {@code true} for a external group
   */
  public void setExternal(boolean external) {
    this.external = external;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * external group
   */
  private boolean external = false;

  /**
   * timestamp of the creation date of this group
   */
  @Indexed
  private Long creationDate;

  /**
   * description of this group
   */
  @Indexed(defaultQuery = true, highlighted = true)
  private String description;

  /**
   * timestamp of the last modified date of this group
   */
  @Indexed
  private Long lastModified;

  /**
   * members of this group
   */
  private List<String> members;

  /**
   * name of this group
   */
  @Indexed(defaultQuery = true, boost = 1.5f)
  private String name;

  /**
   * type of this group
   */
  private String type;

  /**
   * Get the entity name which is used for the audit log
   * @since 2.43.0
   */
  @Override
  public String getEntityName() {
    return getName();
  }
}
