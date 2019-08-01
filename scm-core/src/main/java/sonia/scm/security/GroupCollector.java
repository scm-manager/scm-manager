package sonia.scm.security;

public interface GroupCollector {
  Iterable<String> collect(String principal);
}
