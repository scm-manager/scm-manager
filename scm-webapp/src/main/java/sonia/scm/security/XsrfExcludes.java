package sonia.scm.security;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class XsrfExcludes {

  private final Set<String> excludes = new HashSet<>();

  public void add(String path) {
    excludes.add(path);
  }

  public void remove(String path) {
    excludes.remove(path);
  }

  public boolean contains(String path) {
    return excludes.contains(path);
  }
}
