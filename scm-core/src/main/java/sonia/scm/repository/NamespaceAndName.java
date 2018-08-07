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

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getNamespace() + "/" + getName();
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
