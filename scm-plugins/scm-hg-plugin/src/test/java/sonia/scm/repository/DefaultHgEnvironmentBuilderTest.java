/*
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

package sonia.scm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContext;
import sonia.scm.TransactionId;
import sonia.scm.repository.hooks.HookEnvironment;
import sonia.scm.repository.hooks.HookServer;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.CipherUtil;
import sonia.scm.security.Xsrf;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.DefaultHgEnvironmentBuilder.*;

@ExtendWith(MockitoExtension.class)
class DefaultHgEnvironmentBuilderTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AccessTokenBuilderFactory accessTokenBuilderFactory;

  @Mock
  private HgConfigResolver repositoryConfigResolver;

  @Mock
  private HookEnvironment hookEnvironment;

  @Mock
  private HookServer server;

  @InjectMocks
  private DefaultHgEnvironmentBuilder builder;

  @Mock
  private HgConfig config;

  private Path directory;

  @BeforeEach
  void setBaseDir(@TempDir Path directory) {
    this.directory = directory;
    TempSCMContextProvider context = (TempSCMContextProvider) SCMContext.getContext();
    context.setBaseDirectory(directory.resolve("home").toFile());
  }

  @Test
  void shouldReturnReadEnvironment() {
    Repository heartOfGold = prepareForRead("42");

    Map<String, String> env = builder.read(heartOfGold);
    assertReadEnv(env, "42");
  }

  @Test
  void shouldReturnWriteEnvironment() throws IOException {
    Repository heartOfGold = prepareForWrite("21");

    Map<String, String> env = builder.write(heartOfGold);
    assertReadEnv(env, "21");

    String bearer = CipherUtil.getInstance().decode(env.get(ENV_BEARER_TOKEN));
    assertThat(bearer).isEqualTo("secretAC");
    assertThat(env)
      .containsEntry(ENV_CHALLENGE, "challenge")
      .containsEntry(ENV_HOOK_PORT, "2042");
  }

  @Test
  void shouldSetTransactionId() throws IOException {
    TransactionId.set("ti42");
    Repository heartOfGold = prepareForWrite("21");
    Map<String, String> env = builder.write(heartOfGold);
    assertThat(env).containsEntry(ENV_TRANSACTION_ID, "ti42");
  }

  @Test
  void shouldThrowIllegalStateIfServerCouldNotBeStarted() throws IOException {
    when(server.start()).thenThrow(new IOException("failed to start"));
    Repository repository = prepareForRead("42");
    assertThrows(IllegalStateException.class, () -> builder.write(repository));
  }

  private Repository prepareForWrite(String id) throws IOException {
    Repository heartOfGold = prepareForRead(id);
    applyAccessToken("secretAC");
    when(server.start()).thenReturn(2042);
    when(hookEnvironment.getChallenge()).thenReturn("challenge");
    return heartOfGold;
  }

  private void applyAccessToken(String compact) {
    AccessToken accessToken = mock(AccessToken.class);
    when(accessTokenBuilderFactory.create().custom(Xsrf.TOKEN_KEY, null).build()).thenReturn(accessToken);
    when(accessToken.compact()).thenReturn(compact);
  }


  private void assertReadEnv(Map<String, String> env, String repositoryId) {
    assertThat(env)
      .containsEntry(ENV_REPOSITORY_ID, repositoryId)
      .containsEntry(ENV_REPOSITORY_NAME, "hitchhiker/HeartOfGold")
      .containsEntry(ENV_REPOSITORY_PATH, directory.resolve("repo").toAbsolutePath().toString());
  }

  @Nonnull
  private Repository prepareForRead(String id) {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    heartOfGold.setId(id);

    when(repositoryConfigResolver.resolve(heartOfGold)).thenReturn(config);
    when(config.getDirectory()).thenReturn(directory.resolve("repo").toFile());

    return heartOfGold;
  }

}
