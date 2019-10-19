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
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

@Extension
public class LFSAuthCommand implements CommandInterpreterFactory {

  private static final String LFS_INFO_URL_PATTERN = "%s/repo/%s/%s.git/info/lfs/";

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

    LfsAuthCommandInterpreter(String command) {
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
        ExpiringAction response = createResponseObject(repositoryContext);
        String buffer = serializeResponse(response);
        context.getOutputStream().write(buffer.getBytes());
      };
    }

    @Override
    public RepositoryContextResolver getRepositoryContextResolver() {
      return gitRepositoryContextResolver;
    }

    private ExpiringAction createResponseObject(RepositoryContext repositoryContext) {
      Repository repository = repositoryContext.getRepository();

      String url = format(LFS_INFO_URL_PATTERN, baseUrl, repository.getNamespace(), repository.getName());
      AccessToken accessToken = tokenFactory.getReadAccessToken(repository);

      return new ExpiringAction(url, accessToken);
    }

    private String serializeResponse(ExpiringAction response) throws IOException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      objectMapper.writeValue(buffer, response);
      return buffer.toString();
    }
  }
}
