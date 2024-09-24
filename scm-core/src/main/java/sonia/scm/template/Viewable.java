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

package sonia.scm.template;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A viewable holds the path to a template and the context object which is used to render the template. Viewables can
 * be used as return type of jax-rs resources.
 * 
 * @since 2.0.0
 */
public final class Viewable {
  
  private final String path;
  private final Object context;

  public Viewable(String path, Object context) {
    this.path = path;
    this.context = context;
  }

  public String getPath() {
    return path;
  }

  public Object getContext() {
    return context;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(path, context);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Viewable other = (Viewable) obj;
    return !Objects.equal(this.path, other.path)
      && Objects.equal(this.context, other.context);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("path", path)
            .add("context", context)
            .toString();
  }
  
}
