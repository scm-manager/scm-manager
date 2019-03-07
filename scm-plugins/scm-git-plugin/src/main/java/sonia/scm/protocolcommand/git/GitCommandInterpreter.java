package sonia.scm.protocolcommand.git;

import sonia.scm.protocolcommand.CommandInterpreter;
import sonia.scm.protocolcommand.RepositoryContextResolver;
import sonia.scm.protocolcommand.ScmCommandProtocol;

class GitCommandInterpreter implements CommandInterpreter {
  private final GitRepositoryContextResolver gitRepositoryContextResolver;
  private final GitCommandProtocol gitCommandProtocol;
  private final String[] args;

  GitCommandInterpreter(GitRepositoryContextResolver gitRepositoryContextResolver, GitCommandProtocol gitCommandProtocol, String[] args) {
    this.gitRepositoryContextResolver = gitRepositoryContextResolver;
    this.gitCommandProtocol = gitCommandProtocol;
    this.args = args;
  }

  @Override
  public String[] getParsedArgs() {
    return args;
  }

  @Override
  public ScmCommandProtocol getProtocolHandler() {
    return gitCommandProtocol;
  }

  @Override
  public RepositoryContextResolver getRepositoryContextResolver() {
    return gitRepositoryContextResolver;
  }
}
