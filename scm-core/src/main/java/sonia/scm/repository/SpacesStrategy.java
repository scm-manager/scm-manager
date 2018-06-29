package sonia.scm.repository;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint(multi = true)
public interface SpacesStrategy {
  String getCurrentSpace();
}
