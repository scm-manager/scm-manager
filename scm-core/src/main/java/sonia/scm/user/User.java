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

package sonia.scm.user;

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.BasicPropertiesAware;
import sonia.scm.ModelObject;
import sonia.scm.ReducedModelObject;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.auditlog.AuditLogEntity;
import sonia.scm.search.Indexed;
import sonia.scm.search.IndexedType;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

import java.security.Principal;

@StaticPermissions(
  value = "user",
  globalPermissions = {"create", "list", "autocomplete"},
  permissions = {"read", "modify", "delete", "changePassword", "changePublicKeys", "changeApiKeys"},
  custom = true, customGlobal = true
)
@IndexedType(permission = "user:list")
@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AuditEntry(labels = "user", maskedFields = "password", ignoredFields = "lastModified")
public class User extends BasicPropertiesAware implements Principal, ModelObject, PermissionObject, ReducedModelObject, AuditLogEntity {

  private static final long serialVersionUID = -3089541936726329663L;

  private boolean active = true;
  private boolean external;
  @Indexed
  private Long creationDate;
  @Indexed(defaultQuery = true)
  private String displayName;
  @Indexed
  private Long lastModified;
  @Indexed
  private String mail;
  @Indexed(defaultQuery = true, boost = 1.5f)
  private String name;
  private String password;

  /**
   * The user type is replaced by {@link #external} flag
   *
   * @deprecated Use {@link #external} instead.
   */
  @Deprecated
  private String type;

  public User(String name) {
    this.name = name;
    this.displayName = name;
  }

  public User(String name, String displayName, String mail) {
    this.name = name;
    this.displayName = displayName;
    this.mail = mail;
  }

  public User(String name, String displayName, String mail, String password, String type, boolean active) {
    this.name = name;
    this.displayName = displayName;
    this.mail = mail;
    this.password = password;
    this.type = type;
    this.active = active;
  }

  @Override
  public User clone() {
    User user;

    try {
      user = (User) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }

    return user;
  }

  public boolean copyProperties(User user) {
    return copyProperties(user, true);
  }

  public boolean copyProperties(User user, boolean copyPassword) {
    boolean result = false;

    if (user.isActive() != active) {
      result = true;
      user.setActive(active);
    }

    if (user.isExternal() != external) {
      result = true;
      user.setExternal(external);
    }

    if (Util.isNotEquals(user.getDisplayName(), displayName)) {
      result = true;
      user.setDisplayName(displayName);
    }

    if (Util.isNotEquals(user.getMail(), mail)) {
      result = true;
      user.setMail(mail);
    }

    if (Util.isNotEquals(user.getName(), name)) {
      result = true;
      user.setName(name);
    }

    if (copyPassword && Util.isNotEquals(user.getPassword(), password)) {
      result = true;
      user.setPassword(password);
    }

    if (Util.isNotEquals(user.getType(), type)) {
      result = true;
      user.setType(type);
    }

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final User other = (User) obj;

    return Objects.equal(name, other.name)
      && Objects.equal(displayName, other.displayName)
      && Objects.equal(mail, other.mail)
      && Objects.equal(external, other.external)
      && Objects.equal(active, other.active)
      && Objects.equal(password, other.password)
      && Objects.equal(creationDate, other.creationDate)
      && Objects.equal(lastModified, other.lastModified)
      && Objects.equal(properties, other.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, displayName, mail, password,
      active, external, creationDate, lastModified, properties);
  }

  @Override
  public String toString() {
    String pwd = (password != null)
      ? "(is set)"
      : "(not set)";

    //J-
    return MoreObjects.toStringHelper(this)
      .add("name", name)
      .add("displayName", displayName)
      .add("mail", mail)
      .add("password", pwd)
      .add("type", type)
      .add("active", active)
      .add("external", external)
      .add("creationDate", creationDate)
      .add("lastModified", lastModified)
      .add("properties", properties)
      .toString();
    //J+
  }

  @Override
  public boolean isValid() {
    return ValidationUtil.isNameValid(name) && Util.isNotEmpty(displayName)
      && ((Util.isEmpty(mail)) || ValidationUtil.isMailAddressValid(mail));
    //TODO Ensure that passwords are encrypted
    // && (external || ValidationUtil.isPasswordValid(password));
  }

  @Override
  public String getId() {
    return name;
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
