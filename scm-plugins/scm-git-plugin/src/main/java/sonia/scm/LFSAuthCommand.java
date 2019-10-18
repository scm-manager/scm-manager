package sonia.scm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.protocolcommand.CommandInterpreter;
import sonia.scm.protocolcommand.CommandInterpreterFactory;
import sonia.scm.protocolcommand.RepositoryContextResolver;
import sonia.scm.protocolcommand.ScmCommandProtocol;
import sonia.scm.protocolcommand.git.GitRepositoryContextResolver;
import sonia.scm.repository.Repository;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlElement;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Extension
public class LFSAuthCommand implements CommandInterpreterFactory {

  private final AccessTokenBuilderFactory tokenBuilderFactory;
  private final GitRepositoryContextResolver gitRepositoryContextResolver;
  private final ScmConfiguration configuration;

  @Inject
  public LFSAuthCommand(AccessTokenBuilderFactory tokenBuilderFactory, GitRepositoryContextResolver gitRepositoryContextResolver, ScmConfiguration configuration) {
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.gitRepositoryContextResolver = gitRepositoryContextResolver;
    this.configuration = configuration;
  }

  @Override
  public Optional<CommandInterpreter> canHandle(String command) {
    return command.startsWith("git-lfs-authenticate") ? Optional.of(new CommandInterpreter() {
      @Override
      public String[] getParsedArgs() {
        return new String[] {command.split("\\s+")[1]};
      }

      @Override
      public ScmCommandProtocol getProtocolHandler() {
        return (context, repositoryContext) -> {
          AccessToken accessToken = tokenBuilderFactory.create().expiresIn(5, TimeUnit.MINUTES).build();

          Repository repository = repositoryContext.getRepository();
          String url = format("%s/repo/%s/%s.git/info/lfs/", configuration.getBaseUrl(), repository.getNamespace(), repository.getName());

          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.registerModule(new JaxbAnnotationModule());
          objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:MM:ss'Z'"));
          LfsAuthResponse response = new LfsAuthResponse(url, new LfsAuthHeader(accessToken.compact()), Instant.now().plus(5, ChronoUnit.MINUTES));
          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          objectMapper.writeValue(buffer, response);
          context.getOutputStream().write(buffer.toString().getBytes());
        };
      }

      @Override
      public RepositoryContextResolver getRepositoryContextResolver() {
        return gitRepositoryContextResolver;
      }
    }) : Optional.empty();
  }

  private class LfsAuthResponse {
    private final String href;
    private final LfsAuthHeader header;
    @XmlElement(name = "expires_at")
    private final Date expiresAt;

    public LfsAuthResponse(String href, LfsAuthHeader header, Instant expiresAt) {
      this.href = href;
      this.header = header;
      this.expiresAt = Date.from(expiresAt);
    }

    public String getHref() {
      return href;
    }

    public LfsAuthHeader getHeader() {
      return header;
    }

    public Date getExpiresAt() {
      return expiresAt;
    }
  }

  private class LfsAuthHeader {
    @XmlElement(name = "Authorization")
    private final String authorization;

    public LfsAuthHeader(String authorization) {
      this.authorization = authorization;
    }

    public String getAuthorization() {
      return "Bearer " + authorization;
    }
  }
}
