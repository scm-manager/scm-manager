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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.HgHookMessage;
import sonia.scm.repository.spi.HgHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.security.BearerToken;
import sonia.scm.security.CipherUtil;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import static java.util.Collections.singletonList;

class HgHookHandler implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(HgHookHandler.class);

  private final HgRepositoryHandler handler;
  private final HookEventFacade hookEventFacade;
  private final Provider<HookEnvironment> environmentProvider;
  private final Socket socket;

  HgHookHandler(HgRepositoryHandler handler, HookEventFacade hookEventFacade, Provider<HookEnvironment> environmentProvider, Socket socket) {
    this.handler = handler;
    this.hookEventFacade = hookEventFacade;
    this.environmentProvider = environmentProvider;
    this.socket = socket;
  }

  @Override
  public void run() {
    try (InputStream input = socket.getInputStream(); OutputStream output = socket.getOutputStream()) {
      handleHookRequest(input, output);
    } catch (IOException e) {
      LOG.warn("failed to read hook request", e);
    } finally {
      close();
    }
  }

  private void handleHookRequest(InputStream input, OutputStream output) throws IOException {
    Request request = Sockets.read(input, Request.class);
    Response response = handleHookRequest(request);
    Sockets.send(output, response);
  }

  private Response handleHookRequest(Request request) {
    HookEnvironment environment = environmentProvider.get();
    try {
      if (!environment.isAcceptAble(request.getChallenge())) {
        return error("invalid hook challenge");
      }

      authenticate(request);
      environment.setPending(request.getType() == RepositoryHookType.PRE_RECEIVE);

      HgHookContextProvider context = createHookContextProvider(request);
      hookEventFacade.handle(request.getRepositoryId()).fireHookEvent(request.getType(), context);

      return new Response(context.getHgMessageProvider().getMessages(), false);
    } catch (AuthenticationException ex) {
      LOG.warn("hook authentication failed", ex);
      return error("hook authentication failed");
    } catch (Exception ex) {
      LOG.warn("unknown error on hook occurred", ex);
      return error("unknown error");
    } finally {
      environment.clearPendingState();
    }
  }

  @Nonnull
  private HgHookContextProvider createHookContextProvider(Request request) {
    File repositoryDirectory = handler.getDirectory(request.getRepositoryId());
    return new HgHookContextProvider(
      handler, repositoryDirectory, null, request.node, request.type
    );
  }

  private void authenticate(Request request) {
    String token = CipherUtil.getInstance().decode(request.getToken());
    BearerToken bearer = BearerToken.valueOf(token);
    Subject subject = SecurityUtils.getSubject();
    subject.login(bearer);
  }

  private Response error(String message) {
    return new Response(
      singletonList(new HgHookMessage(HgHookMessage.Severity.ERROR, message)),
      true
    );
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
