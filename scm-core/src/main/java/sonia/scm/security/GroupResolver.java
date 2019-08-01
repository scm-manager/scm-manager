package sonia.scm.security;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface GroupResolver {

  Iterable<String> resolveGroups(String principal);
}
