package sonia.scm.group;

import sonia.scm.plugin.ExtensionPoint;

import java.util.Set;

@ExtensionPoint
public interface GroupResolver {
  Set<String> resolve(String principal);
}
