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

package sonia.scm.repository;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Objects;

public class NamespaceAndName implements Comparable<NamespaceAndName> {

  private final String namespace;
  private final String name;

  public NamespaceAndName(String namespace, String name) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(namespace), "a non empty namespace is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "a non empty name is required");
    this.namespace = namespace;
    this.name = name;
  }

  /**
   * @since 2.38.0
   */
  public static NamespaceAndName fromString(String namespaceAndName) {
    String[] parts = namespaceAndName.split("/");
    if (parts.length != 2) {
      throw new IllegalArgumentException("namespace and name must be divided by a slash (/)");
    }
    return new NamespaceAndName(parts[0], parts[1]);
  }

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  public String logString() {
    return getNamespace() + "/" + getName();
  }

  @Override
  public String toString() {
    return logString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NamespaceAndName that = (NamespaceAndName) o;
    return Objects.equals(namespace, that.namespace) &&
      Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, name);
  }

  @Override
  public int compareTo(NamespaceAndName o) {
    int result = namespace.compareTo(o.namespace);
    if (result == 0) {
      return name.compareTo(o.name);
    }
    return result;
  }
}
