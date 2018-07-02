package sonia.scm.repository;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface NamespaceStrategy {
  String getNamespace();
}
