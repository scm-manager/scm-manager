package sonia.scm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import sonia.scm.plugin.Extension;
import sonia.scm.protocolcommand.CommandInterpreter;
import sonia.scm.protocolcommand.CommandInterpreterFactory;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.protocolcommand.RepositoryContextResolver;
import sonia.scm.protocolcommand.ScmCommandProtocol;
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

@Extension
public class LFSAuthCommand implements CommandInterpreterFactory {

  private final AccessTokenBuilderFactory tokenBuilderFactory;

  @Inject
  public LFSAuthCommand(AccessTokenBuilderFactory tokenBuilderFactory) {
    this.tokenBuilderFactory = tokenBuilderFactory;
  }

  @Override
  public Optional<CommandInterpreter> canHandle(String command) {
    return command.startsWith("git-lfs-authenticate") ? Optional.of(new CommandInterpreter() {
      @Override
      public String[] getParsedArgs() {
        return new String[0];
      }

      @Override
      public ScmCommandProtocol getProtocolHandler() {
        return (context, repositoryContext) -> {
          AccessToken accessToken = tokenBuilderFactory.create().expiresIn(5, TimeUnit.MINUTES).build();

          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.registerModule(new JaxbAnnotationModule());
          objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:MM:ss'Z'"));
          LfsAuthResponse response = new LfsAuthResponse("http://localhost:8081/scm/repo/scmadmin/lfs.git/info/lfs/", new LfsAuthHeader(accessToken.compact()), Instant.now().plus(5, ChronoUnit.MINUTES));
          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          objectMapper.writeValue(buffer, response);
          context.getOutputStream().write(buffer.toString().getBytes());
        };
      }

      @Override
      public RepositoryContextResolver getRepositoryContextResolver() {
        return args -> new RepositoryContext(null, null);
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
