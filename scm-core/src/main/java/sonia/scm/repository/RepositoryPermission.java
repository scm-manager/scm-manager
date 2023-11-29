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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.commons.collections.CollectionUtils;
import sonia.scm.security.PermissionObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

//~--- JDK imports ------------------------------------------------------------

/**
 * Permissions controls the access to {@link Repository}.
 * This object should be immutable, but could not be due to mapstruct. Do not modify instances of this because this
 * would change the hash code and therefor make it undeletable in a repository.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "permissions")
@XmlAccessorType(XmlAccessType.FIELD)
public class RepositoryPermission implements PermissionObject, Serializable
{

  private static final long serialVersionUID = -2915175031430884040L;
  public static final String REPOSITORY_MODIFIED_EXCEPTION_TEXT = "repository permission must not be modified";

  private Boolean groupPermission;
  private String name;
  @XmlElement(name = "verb")
  private Set<String> verbs;
  private String role;

  /**
   * This constructor exists for mapstruct and JAXB, only -- <b>do not use this in "normal" code</b>.
   *
   * @deprecated Do not use this for "normal" code.
   * Use {@link RepositoryPermission#RepositoryPermission(String, Collection, boolean)} instead.
   */
  @Deprecated
  public RepositoryPermission() {}

  public RepositoryPermission(String name, Collection<String> verbs, boolean groupPermission)
  {
    this.name = name;
    this.verbs = new LinkedHashSet<>(verbs);
    this.role = null;
    this.groupPermission = groupPermission;
  }

  public RepositoryPermission(String name, String role, boolean groupPermission)
  {
    this.name = name;
    this.verbs = emptySet();
    this.role = role;
    this.groupPermission = groupPermission;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns true if the {@link RepositoryPermission} is the same as the obj argument.
   *
   *
   * @param obj the reference object with which to compare
   *
   * @return true if the {@link RepositoryPermission} is the same as the obj argument
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final RepositoryPermission other = (RepositoryPermission) obj;

    return Objects.equal(name, other.name)
      && equalVerbs(other)
      && Objects.equal(role, other.role)
      && Objects.equal(groupPermission, other.groupPermission);
  }

  public boolean equalVerbs(RepositoryPermission other) {
    return verbs == null && other.verbs == null
      || verbs != null && other.verbs != null && CollectionUtils.isEqualCollection(verbs, other.verbs);
  }

  /**
   * Returns the hash code value for the {@link RepositoryPermission}.
   *
   *
   * @return the hash code value for the {@link RepositoryPermission}
   */
  @Override
  public int hashCode()
  {
    // Normally we do not have a log of repository permissions having the same size of verbs, but different content.
    // Therefore we do not use the verbs themselves for the hash code but only the number of verbs.
    return Objects.hashCode(name, verbs == null? -1: verbs.size(), role, groupPermission);
  }


  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("role", role)
            .add("verbs", verbs)
            .add("groupPermission", groupPermission)
            .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the name of the user or group.
   *
   *
   * @return name of the user or group
   */
  @Override
  public String getName()
  {
    return name;
  }

  /**
   * Returns the verb of the permission.
   *
   *
   * @return verb of the permission
   */
  public Collection<String> getVerbs()
  {
    return verbs == null ? emptyList() : Collections.unmodifiableSet(verbs);
  }

  /**
   * Returns the role of the permission.
   *
   *
   * @return role of the permission
   */
  public String getRole() {
    return role;
  }

  /**
   * Returns true if the permission is a permission which affects a group.
   *
   *
   * @return true if the permision is a group permission
   */
  @Override
  public boolean isGroupPermission()
  {
    return groupPermission;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Use this for creation only. This will throw an {@link IllegalStateException} when modified.
   * @throws IllegalStateException when modified after the value has been set once.
   *
   * @deprecated Do not use this for "normal" code.
   * Use {@link RepositoryPermission#RepositoryPermission(String, Collection, boolean)}
   * or {@link RepositoryPermission#RepositoryPermission(String, String, boolean)} instead.
   */
  @Deprecated
  public void setGroupPermission(boolean groupPermission)
  {
    if (this.groupPermission != null) {
      throw new IllegalStateException(REPOSITORY_MODIFIED_EXCEPTION_TEXT);
    }
    this.groupPermission = groupPermission;
  }

  /**
   * Use this for creation only. This will throw an {@link IllegalStateException} when modified.
   * @throws IllegalStateException when modified after the value has been set once.
   *
   * @deprecated Do not use this for "normal" code.
   * Use {@link RepositoryPermission#RepositoryPermission(String, Collection, boolean)}
   * or {@link RepositoryPermission#RepositoryPermission(String, String, boolean)} instead.
   */
  @Deprecated
  public void setName(String name)
  {
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
   * Use {@link RepositoryPermission#RepositoryPermission(String, String, boolean)} instead.
   */
  @Deprecated
  public void setRole(String role)
  {
    if (this.role != null) {
      throw new IllegalStateException(REPOSITORY_MODIFIED_EXCEPTION_TEXT);
    }
    this.role = role;
  }

  /**
   * Use this for creation only. This will throw an {@link IllegalStateException} when modified.
   * @throws IllegalStateException when modified after the value has been set once.
   *
   * @deprecated Do not use this for "normal" code.
   * Use {@link RepositoryPermission#RepositoryPermission(String, Collection, boolean)} instead.
   */
  @Deprecated
  public void setVerbs(Collection<String> verbs)
  {
    if (this.verbs != null) {
      throw new IllegalStateException(REPOSITORY_MODIFIED_EXCEPTION_TEXT);
    }
    this.verbs = verbs == null? emptySet(): unmodifiableSet(new LinkedHashSet<>(verbs));
  }
}
