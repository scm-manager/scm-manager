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

package sonia.scm.work;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
final class Resource implements Serializable {

  private final String name;
  @Nullable
  private final String id;

  Resource(String name) {
    this.name = name;
    this.id = null;
  }

  Resource(String name, @Nullable String id) {
    this.name = name;
    this.id = id;
  }

  boolean isBlockedBy(Resource resource) {
    if (name.equals(resource.name)) {
      if (id != null && resource.id != null) {
        return id.equals(resource.id);
      }
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    if (id != null) {
      return name + ":" + id;
    }
    return name;
  }
}
