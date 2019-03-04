package sonia.scm.protocolcommand;

import java.io.IOException;

public interface ScmSshProtocol {

  void handle(CommandContext context, RepositoryContext repositoryContext) throws IOException;

}
