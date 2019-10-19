package sonia.scm.web.lfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.protocolcommand.CommandInterpreter;
import sonia.scm.protocolcommand.CommandInterpreterFactory;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.protocolcommand.RepositoryContextResolver;
import sonia.scm.protocolcommand.ScmCommandProtocol;
import sonia.scm.protocolcommand.git.GitRepositoryContextResolver;
import sonia.scm.repository.Repository;
import sonia.scm.security.AccessToken;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static java.lang.String.format;

@Extension
public class LFSAuthCommand implements CommandInterpreterFactory {

  private final LfsAccessTokenFactory tokenFactory;
  private final GitRepositoryContextResolver gitRepositoryContextResolver;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  @Inject
  public LFSAuthCommand(LfsAccessTokenFactory tokenFactory, GitRepositoryContextResolver gitRepositoryContextResolver, ScmConfiguration configuration) {
    this.tokenFactory = tokenFactory;
    this.gitRepositoryContextResolver = gitRepositoryContextResolver;

    objectMapper = new ObjectMapper();
    baseUrl = configuration.getBaseUrl();
  }

  @Override
  public Optional<CommandInterpreter> canHandle(String command) {
    if (command.startsWith("git-lfs-authenticate")) {
      return Optional.of(new LfsAuthCommandInterpreter(command));
    } else {
      return Optional.empty();
    }
  }

  private class LfsAuthCommandInterpreter implements CommandInterpreter {

    private final String command;

    public LfsAuthCommandInterpreter(String command) {
      this.command = command;
    }

    @Override
    public String[] getParsedArgs() {
      // we are interested only in the 'repo' argument, so we discard the rest
      return new String[]{command.split("\\s+")[1]};
    }

    @Override
    public ScmCommandProtocol getProtocolHandler() {
      return (context, repositoryContext) -> {
        ExpiringAction response = createResponse(repositoryContext);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        objectMapper.writeValue(buffer, response);
        context.getOutputStream().write(buffer.toString().getBytes());
      };
    }

    private ExpiringAction createResponse(RepositoryContext repositoryContext) {
      Repository repository = repositoryContext.getRepository();

      String url = format("%s/repo/%s/%s.git/info/lfs/", baseUrl, repository.getNamespace(), repository.getName());
      AccessToken accessToken = tokenFactory.getReadAccessToken(repository);

      return new ExpiringAction(url, accessToken);
    }

    @Override
    public RepositoryContextResolver getRepositoryContextResolver() {
      return gitRepositoryContextResolver;
    }
  }
}
