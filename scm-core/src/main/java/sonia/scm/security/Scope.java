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

package sonia.scm.security;

import com.google.common.collect.ImmutableList;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Scope of a token. A scope is able to reduce the permissions of a token authentication. That means we can issue a
 * token which is only suitable for a single or a set of actions e.g.: we could create a token which can only read
 * a single repository. The values of the scope should be explicit string representations of shiro permissions. An empty
 * scope means all permissions of the user.
 *
 * @since 2.0.0
 */
@XmlRootElement(name = "scope")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Scope implements Iterable<String> {

  private static final Scope EMPTY = new Scope(Collections.emptySet());

  private final Collection<String> values;

  private Scope(Collection<String> values) {
    this.values = values;
  }

  @Override
  public Iterator<String> iterator() {
    return values.iterator();
  }

  /**
   * Returns {@code true} if the scope is empty.
   */
  public boolean isEmpty() {
    return values.isEmpty();
  }

  /**
   * Creates an empty scope.
   */
  public static Scope empty(){
    return EMPTY;
  }

  /**
   * Creates a scope object from the given iterable.
   *
   * @param values values of scope
   *
   * @return new scope
   */
  public static Scope valueOf(Iterable<String> values) {
    return new Scope(ImmutableList.copyOf(values));
  }

  /**
   * Create a scope from the given array.
   *
   * @param values values of scope
   *
   * @return new scope.
   */
  public static Scope valueOf(String... values) {
    return new Scope(ImmutableList.copyOf(values));
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("[");
    Iterator<String> it = values.iterator();
    while (it.hasNext()) {
      buffer.append('"').append(it.next()).append('"');
      if (it.hasNext()) {
        buffer.append(", ");
      }
    }
    return buffer.append("]").toString();
  }

}
