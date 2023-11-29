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

import com.google.inject.assistedinject.Assisted;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ExceptionWithContext;
import sonia.scm.NotFoundException;
import sonia.scm.TransactionId;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.HgHookMessage;
import sonia.scm.repository.spi.HgHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.security.BearerToken;
import sonia.scm.security.CipherUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

class DefaultHookHandler implements HookHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultHookHandler.class);

  private final HookEventFacade hookEventFacade;
  private final HookEnvironment environment;
  private final HookContextProviderFactory hookContextProviderFactory;
  private final Socket socket;

  @Inject
  public DefaultHookHandler(HookContextProviderFactory hookContextProviderFactory, HookEventFacade hookEventFacade, HookEnvironment environment, @Assisted Socket socket) {
    this.hookContextProviderFactory = hookContextProviderFactory;
    this.hookEventFacade = hookEventFacade;
    this.environment = environment;
    this.socket = socket;
  }

  @Override
  public void run() {
    LOG.trace("start handling hook protocol");
    try (InputStream input = socket.getInputStream(); OutputStream output = socket.getOutputStream()) {
      handleHookRequest(input, output);
    } catch (IOException e) {
      LOG.warn("failed to read hook request", e);
    } finally {
      LOG.trace("close client socket");
      TransactionId.clear();
      close();
    }
  }

  private void handleHookRequest(InputStream input, OutputStream output) throws IOException {
    Request request = Sockets.receive(input, Request.class);
    TransactionId.set(request.getTransactionId());
    Response response = handleHookRequest(request);
    Sockets.send(output, response);
  }

  private Response handleHookRequest(Request request) {
    LOG.trace("process {} hook for node {}", request.getType(), request.getNode());

    if (!environment.isAcceptAble(request.getChallenge())) {
      LOG.warn("received hook with invalid challenge: {}", request.getChallenge());
      return error("invalid hook challenge");
    }

    try {
      authenticate(request);

      return fireHook(request);
    } catch (AuthenticationException ex) {
      LOG.warn("hook authentication failed", ex);
      return error("hook authentication failed");
    }
  }

  @Nonnull
  private Response fireHook(Request request) {
    HgHookContextProvider context = hookContextProviderFactory.create(request.getRepositoryId(), request.getNode());

    try {
      environment.setPending(request.getType() == RepositoryHookType.PRE_RECEIVE);

      hookEventFacade.handle(request.getRepositoryId()).fireHookEvent(request.getType(), context);

      return new Response(context.getHgMessageProvider().getMessages(), false);

    } catch (NotFoundException ex) {
      LOG.warn("could not find repository with id {}", request.getRepositoryId(), ex);
      return error("repository not found");
    } catch (ExceptionWithContext ex) {
      LOG.debug("scm exception on hook occurred", ex);
      return error(context, ex.getMessage());
    } catch (Exception ex) {
      LOG.warn("unknown error on hook occurred", ex);
      return error(context, "unknown error");
    } finally {
      environment.clearPendingState();
    }
  }

  private void authenticate(Request request) {
    LOG.trace("authenticate hook request");
    String token = CipherUtil.getInstance().decode(request.getToken());
    BearerToken bearer = BearerToken.valueOf(token);
    Subject subject = SecurityUtils.getSubject();
    subject.login(bearer);
  }

  private Response error(HgHookContextProvider context, String message) {
    List<HgHookMessage> messages = new ArrayList<>(context.getHgMessageProvider().getMessages());
    messages.add(createErrorMessage(message));
    return new Response(messages, true);
  }

  private Response error(String message) {
    return new Response(
      singletonList(createErrorMessage(message)),
      true
    );
  }

  @Nonnull
  private HgHookMessage createErrorMessage(String message) {
    return new HgHookMessage(HgHookMessage.Severity.ERROR, message);
  }

  private void close() {
    try {
      socket.close();
    } catch (IOException e) {
      LOG.debug("failed to close hook socket", e);
    }
  }

  @Data
  @AllArgsConstructor
  public static class Request {
    private String token;
    private RepositoryHookType type;
    private String transactionId;
    private String repositoryId;
    private String challenge;
    private String node;
  }

  @Data
  @AllArgsConstructor
  public static class Response {
    private List<HgHookMessage> messages;
    private boolean abort;
  }
}
