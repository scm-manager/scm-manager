package sonia.scm.security;

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
    return name.equals(that.name)
      && this.verbs.containsAll(that.verbs)
      && this.verbs.size() == that.verbs.size();
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, verbs.size());
  }
}
