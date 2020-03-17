/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.web.lfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.protocolcommand.CommandContext;
import sonia.scm.protocolcommand.CommandInterpreter;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.protocolcommand.git.GitRepositoryContextResolver;
import sonia.scm.repository.Repository;
import sonia.scm.security.AccessToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static java.time.Instant.parse;
import static java.util.Date.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LFSAuthCommandTest {

  static final Repository REPOSITORY = new Repository("1", "git", "space", "X");
  static final Date EXPIRATION = from(parse("2007-05-03T10:15:30.00Z"));

  @Mock
  LfsAccessTokenFactory tokenFactory;
  @Mock
  GitRepositoryContextResolver gitRepositoryContextResolver;
  @Mock
  ScmConfiguration configuration;

  @InjectMocks
  LFSAuthCommand lfsAuthCommand;

  @BeforeEach
  void initAuthorizationToken() {
    AccessToken accessToken = mock(AccessToken.class);
    lenient().when(this.tokenFactory.createReadAccessToken(REPOSITORY)).thenReturn(accessToken);
    lenient().when(this.tokenFactory.createWriteAccessToken(REPOSITORY)).thenReturn(accessToken);
    lenient().when(accessToken.getExpiration()).thenReturn(EXPIRATION);
    lenient().when(accessToken.compact()).thenReturn("ACCESS_TOKEN");
  }

  @BeforeEach
  void initConfig() {
    lenient().when(configuration.getBaseUrl()).thenReturn("http://example.com");
  }

  @Test
  void shouldHandleGitLfsAuthenticate() {
    Optional<CommandInterpreter> commandInterpreter = lfsAuthCommand.canHandle("git-lfs-authenticate repo/space/X upload");
    assertThat(commandInterpreter).isPresent();
  }

  @Test
  void shouldNotHandleOtherCommands() {
    Optional<CommandInterpreter> commandInterpreter = lfsAuthCommand.canHandle("git-lfs-something repo/space/X upload");
    assertThat(commandInterpreter).isEmpty();
  }

  @Test
  void shouldExtractRepositoryArgument() {
    CommandInterpreter commandInterpreter = lfsAuthCommand.canHandle("git-lfs-authenticate  repo/space/X\t upload").get();
    assertThat(commandInterpreter.getParsedArgs()).containsOnly("repo/space/X");
  }

  @Test
  void shouldCreateJsonResponse() throws IOException {
    CommandInterpreter commandInterpreter = lfsAuthCommand.canHandle("git-lfs-authenticate  repo/space/X\t upload").get();
    CommandContext commandContext = createCommandContext();
    commandInterpreter.getProtocolHandler().handle(commandContext, createRepositoryContext());
    assertThat(commandContext.getOutputStream().toString())
      .isEqualTo("{\"href\":\"http://example.com/repo/space/X.git/info/lfs/\",\"header\":{\"Authorization\":\"Bearer ACCESS_TOKEN\"},\"expires_at\":\"2007-05-03T10:15:30Z\"}");
  }

  private CommandContext createCommandContext() {
    return new CommandContext(null, null, null, new ByteArrayOutputStream(), null);
  }

  private RepositoryContext createRepositoryContext() {
    return new RepositoryContext(REPOSITORY, null);
  }
}
