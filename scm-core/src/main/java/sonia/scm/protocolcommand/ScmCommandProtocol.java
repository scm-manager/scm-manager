package sonia.scm.protocolcommand;

import sonia.scm.plugin.ExtensionPoint;

import java.io.IOException;

@ExtensionPoint
public interface ScmCommandProtocol {

  boolean canHandle(RepositoryContext repositoryContext);

  void handle(CommandContext context, RepositoryContext repositoryContext) throws IOException;

}
