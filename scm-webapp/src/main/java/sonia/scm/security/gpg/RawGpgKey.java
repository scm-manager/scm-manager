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

package sonia.scm.security.gpg;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.repository.Person;
import sonia.scm.xml.XmlInstantAdapter;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class RawGpgKey {

  private String id;
  private String displayName;
  private String owner;
  private String raw;
  private Set<Person> contacts;

  @XmlJavaTypeAdapter(XmlInstantAdapter.class)
  private Instant created;

  private boolean readonly;

  RawGpgKey(String id) {
    this.id = id;
  }
  RawGpgKey(String id, String raw) {
    this.id = id;
    this.raw = raw;
  }
  RawGpgKey(String id, String displayName, String owner, String raw, Set<Person> contacts, Instant created) {
    this.id = id;
    this.displayName = displayName;
    this.owner = owner;
    this.contacts = contacts;
    this.created = created;
    this.raw = raw;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RawGpgKey that = (RawGpgKey) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
