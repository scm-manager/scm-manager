package sonia.scm.migration;

import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.version.Version;

@ExtensionPoint
public interface UpdateStep {
  void doUpdate();

  Version getTargetVersion();

  String affectedDataType();
}
