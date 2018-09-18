package sonia.scm.repository.api;

import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Repository;

@ExtensionPoint(multi = true)
public interface ScmProtocolProvider<T extends ScmProtocol> {

  String getType();

  T get(Repository repository);
}
