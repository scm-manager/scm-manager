/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web.lfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

@Extension
public class LFSAuthCommand implements CommandInterpreterFactory {

  private static final Logger LOG = LoggerFactory.getLogger(LFSAuthCommand.class);

  private static final String LFS_INFO_URL_PATTERN = "%s/repo/%s/%s.git/info/lfs/";

  private final LfsAccessTokenFactory tokenFactory;
  private final GitRepositoryContextResolver gitRepositoryContextResolver;
  private final ObjectMapper objectMapper;
  private final ScmConfiguration configuration;

  @Inject
  public LFSAuthCommand(LfsAccessTokenFactory tokenFactory, GitRepositoryContextResolver gitRepositoryContextResolver, ScmConfiguration configuration) {
    this.tokenFactory = tokenFactory;
    this.gitRepositoryContextResolver = gitRepositoryContextResolver;

    objectMapper = new ObjectMapper();
    this.configuration = configuration;
  }

  @Override
  public Optional<CommandInterpreter> canHandle(String command) {
    if (command.startsWith("git-lfs-authenticate")) {
      LOG.trace("create command for input: {}", command);
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
        // we buffer the response and write it with a single write,
        // because otherwise the ssh connection is not closed
        String buffer = serializeResponse(response);
        context.getOutputStream().write(buffer.getBytes(Charsets.UTF_8));
      };
    }

    @Override
    public RepositoryContextResolver getRepositoryContextResolver() {
      return gitRepositoryContextResolver;
    }

    private ExpiringAction createResponseObject(RepositoryContext repositoryContext) {
      Repository repository = repositoryContext.getRepository();

      String url = format(LFS_INFO_URL_PATTERN, configuration.getBaseUrl(), repository.getNamespace(), repository.getName());
      AccessToken accessToken = tokenFactory.createReadAccessToken(repository);

      return new ExpiringAction(url, accessToken);
    }

    private String serializeResponse(ExpiringAction response) throws IOException {
      return objectMapper.writeValueAsString(response);
    }
  }
}
