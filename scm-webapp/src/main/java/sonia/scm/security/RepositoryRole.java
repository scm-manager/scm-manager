package sonia.scm.security;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class RepositoryRole {

  private final String name;
  private final Collection<String> verbs;

  public RepositoryRole(String name, Collection<String> verbs) {
    this.name = name;
    this.verbs = verbs;
  }

  public String getName() {
    return name;
  }

  public Collection<String> getVerbs() {
    return Collections.unmodifiableCollection(verbs);
  }

  public String toString() {
    return "Role " + name + " (" + String.join(", ", verbs) + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RepositoryRole)) return false;
    RepositoryRole that = (RepositoryRole) o;
    return name.equals(that.name) &&
      CollectionUtils.isEqualCollection(this.verbs, that.verbs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, verbs.size());
  }
}
