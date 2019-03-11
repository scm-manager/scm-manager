package sonia.scm.protocolcommand.git;

import sonia.scm.plugin.Extension;
import sonia.scm.protocolcommand.CommandInterpreter;
import sonia.scm.protocolcommand.CommandInterpreterFactory;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Extension
public class GitCommandInterpreterFactory implements CommandInterpreterFactory {
  private final GitCommandProtocol gitCommandProtocol;
  private final GitRepositoryContextResolver gitRepositoryContextResolver;

  @Inject
  public GitCommandInterpreterFactory(GitCommandProtocol gitCommandProtocol, GitRepositoryContextResolver gitRepositoryContextResolver) {
    this.gitCommandProtocol = gitCommandProtocol;
    this.gitRepositoryContextResolver = gitRepositoryContextResolver;
  }

  @Override
  public Optional<CommandInterpreter> canHandle(String command) {
    try {
      String[] args = GitCommandParser.parse(command);
      if (args[0].startsWith("git")) {
        return of(new GitCommandInterpreter(gitRepositoryContextResolver, gitCommandProtocol, args));
      } else {
        return empty();
      }
    } catch (IllegalArgumentException e) {
      return empty();
    }
  }
}
