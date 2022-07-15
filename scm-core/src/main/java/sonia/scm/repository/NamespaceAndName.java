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
