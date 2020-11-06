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

package sonia.scm.repository.hooks;

import com.google.inject.util.Providers;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.HgHookMessage;
import sonia.scm.repository.spi.HgHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.security.CipherUtil;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HgHookHandlerTest {

  @Mock
  private HgRepositoryHandler repositoryHandler;

  @Mock
  private HookEventFacade hookEventFacade;

  @Mock
  private HookEventFacade.HookEventHandler hookEventHandler;

  @Mock
  private Socket socket;

  private HookEnvironment hookEnvironment;

  private HgHookHandler handler;

  @Mock
  private Subject subject;

  @BeforeEach
  void setUp() {
    ThreadContext.bind(subject);

    hookEnvironment = new HookEnvironment();
    DefaultHookHandlerFactory factory = new DefaultHookHandlerFactory(
      repositoryHandler, hookEventFacade, Providers.of(hookEnvironment)
    );

    handler = factory.create(socket);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldFireHook() throws IOException {
    when(hookEventFacade.handle("42")).thenReturn(hookEventHandler);

    HgHookHandler.Request request = createRequest(RepositoryHookType.POST_RECEIVE);
    HgHookHandler.Response response = send(request);

    assertSuccess(response, RepositoryHookType.POST_RECEIVE);
    assertThat(hookEnvironment.isPending()).isFalse();
  }

  @Test
  void shouldSetPendingStateOnPreReceiveHooks() throws IOException {
    when(hookEventFacade.handle("42")).thenReturn(hookEventHandler);

    HgHookHandler.Request request = createRequest(RepositoryHookType.PRE_RECEIVE);
    HgHookHandler.Response response = send(request);

    assertSuccess(response, RepositoryHookType.PRE_RECEIVE);
    assertThat(hookEnvironment.isPending()).isTrue();
  }

  @Test
  void shouldHandleAuthenticationFailure() throws IOException {
    doThrow(IllegalStateException.class)
      .when(hookEventFacade)
      .handle("42");

    HgHookHandler.Request request = createRequest(RepositoryHookType.POST_RECEIVE);
    HgHookHandler.Response response = send(request);

    assertError(response, "unknown");
  }

  @Test
  void shouldHandleUnknownFailure() throws IOException {
    doThrow(AuthenticationException.class)
      .when(subject)
      .login(any(AuthenticationToken.class));

    HgHookHandler.Request request = createRequest(RepositoryHookType.POST_RECEIVE);
    HgHookHandler.Response response = send(request);

    assertError(response, "authentication");
  }

  @Test
  void shouldReturnErrorWithInvalidChallenge() throws IOException {
    HgHookHandler.Request request = createRequest(RepositoryHookType.POST_RECEIVE, "something-different");
    HgHookHandler.Response response = send(request);

    assertError(response, "challenge");
  }

  private void assertSuccess(HgHookHandler.Response response, RepositoryHookType type) {
    assertThat(response.getMessages()).isEmpty();
    assertThat(response.isAbort()).isFalse();

    verify(hookEventHandler).fireHookEvent(eq(type), any(HgHookContextProvider.class));
  }

  private void assertError(HgHookHandler.Response response, String message) {
    assertThat(response.isAbort()).isTrue();
    assertThat(response.getMessages()).hasSize(1);
    HgHookMessage hgHookMessage = response.getMessages().get(0);
    assertThat(hgHookMessage.getSeverity()).isEqualTo(HgHookMessage.Severity.ERROR);
    assertThat(hgHookMessage.getMessage()).contains(message);
  }

  @Nonnull
  private HgHookHandler.Request createRequest(RepositoryHookType type) {
    return createRequest(type, hookEnvironment.getChallenge());
  }

  @Nonnull
  private HgHookHandler.Request createRequest(RepositoryHookType type, String challenge) {
    String secret = CipherUtil.getInstance().encode("secret");
    return new HgHookHandler.Request(
      secret, type, "42", challenge, "abc"
    );
  }

  private HgHookHandler.Response send(HgHookHandler.Request request) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    Sockets.send(buffer, request);
    ByteArrayInputStream input = new ByteArrayInputStream(buffer.toByteArray());
    when(socket.getInputStream()).thenReturn(input);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    when(socket.getOutputStream()).thenReturn(output);

    handler.run();

    return Sockets.read(new ByteArrayInputStream(output.toByteArray()), HgHookHandler.Response.class);
  }

}
