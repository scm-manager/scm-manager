package sonia.scm.migration;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface UpdateStep {
  void doUpdate();

  String getTargetVersion();
}
