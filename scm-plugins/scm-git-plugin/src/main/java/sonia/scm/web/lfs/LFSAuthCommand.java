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
import sonia.scm.security.AccessTokenBuilderFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Extension
public class LFSAuthCommand implements CommandInterpreterFactory {

  private final AccessTokenBuilderFactory tokenBuilderFactory;
  private final GitRepositoryContextResolver gitRepositoryContextResolver;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  @Inject
  public LFSAuthCommand(AccessTokenBuilderFactory tokenBuilderFactory, GitRepositoryContextResolver gitRepositoryContextResolver, ScmConfiguration configuration) {
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.gitRepositoryContextResolver = gitRepositoryContextResolver;

    objectMapper = new ObjectMapper();
    baseUrl = configuration.getBaseUrl();
  }

  @Override
  public Optional<CommandInterpreter> canHandle(String command) {
    return command.startsWith("git-lfs-authenticate") ? Optional.of(new CommandInterpreter() {
      @Override
      public String[] getParsedArgs() {
        // we are interested only in the 'repo' argument, so we discard the rest
        return new String[] {command.split("\\s+")[1]};
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
        AccessToken accessToken = tokenBuilderFactory.create().expiresIn(5, TimeUnit.MINUTES).build();

        Repository repository = repositoryContext.getRepository();
        String url = format("%s/repo/%s/%s.git/info/lfs/", baseUrl, repository.getNamespace(), repository.getName());

        return new ExpiringAction(url, accessToken);
      }

      @Override
      public RepositoryContextResolver getRepositoryContextResolver() {
        return gitRepositoryContextResolver;
      }
    }) : Optional.empty();
  }
}
