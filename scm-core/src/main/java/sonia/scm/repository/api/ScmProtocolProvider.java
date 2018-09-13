package sonia.scm.repository.api;

import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Repository;

@ExtensionPoint(multi = true)
public interface ScmProtocolProvider {

  String getType();

  ScmProtocol get(Repository repository);
}
