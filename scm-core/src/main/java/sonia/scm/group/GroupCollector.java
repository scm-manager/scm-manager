package sonia.scm.group;

import java.util.Set;

public interface GroupCollector {

  String AUTHENTICATED = "_authenticated";

  Set<String> collect(String principal);
}
