package sonia.scm.protocolcommand;

import java.io.IOException;

public interface ScmCommandProtocol {

  void handle(CommandContext context, RepositoryContext repositoryContext) throws IOException;

}
