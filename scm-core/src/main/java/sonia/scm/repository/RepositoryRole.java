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

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import sonia.scm.ModelObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
@StaticPermissions(value = "repositoryRole", permissions = {}, globalPermissions = {"read", "modify"})
@XmlRootElement(name = "roles")
@XmlAccessorType(XmlAccessType.FIELD)
public class RepositoryRole implements ModelObject, PermissionObject {

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

  /**
   * Returns true if the {@link RepositoryRole} is the same as the obj argument.
   *
   *
   * @param obj the reference object with which to compare
   *
   * @return true if the {@link RepositoryRole} is the same as the obj argument
   */
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
   *
   *
   * @return the hash code value for the {@link RepositoryRole}
   */
  @Override
  public int hashCode() {
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
}
