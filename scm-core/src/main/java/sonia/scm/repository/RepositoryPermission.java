/*
  Copyright (c) 2010, Sebastian Sdorra
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright notice,
     this list of conditions and the following disclaimer in the documentation
     and/or other materials provided with the distribution.
  3. Neither the name of SCM-Manager; nor the names of its
     contributors may be used to endorse or promote products derived from this
     software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  http://bitbucket.org/sdorra/scm-manager

 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import sonia.scm.security.PermissionObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.emptyList;
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
      && verbs.containsAll(other.verbs)
      && verbs.size() == other.verbs.size()
      && Objects.equal(groupPermission, other.groupPermission);
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
    return Objects.hashCode(name, verbs.size(), groupPermission);
  }


  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
            .add("name", name)
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
   * Use {@link RepositoryPermission#RepositoryPermission(String, Collection, boolean)} instead.
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
   * Use {@link RepositoryPermission#RepositoryPermission(String, Collection, boolean)} instead.
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
   * Use {@link RepositoryPermission#RepositoryPermission(String, Collection, boolean)} instead.
   */
  @Deprecated
  public void setVerbs(Collection<String> verbs)
  {
    if (this.verbs != null) {
      throw new IllegalStateException(REPOSITORY_MODIFIED_EXCEPTION_TEXT);
    }
    this.verbs = unmodifiableSet(new LinkedHashSet<>(verbs));
  }
}
