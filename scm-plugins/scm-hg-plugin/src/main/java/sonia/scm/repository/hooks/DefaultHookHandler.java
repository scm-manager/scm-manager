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
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.HgHookMessage;
import sonia.scm.repository.spi.HgHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.security.BearerToken;
import sonia.scm.security.CipherUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
    try {
      if (!environment.isAcceptAble(request.getChallenge())) {
        return error("invalid hook challenge");
      }

      authenticate(request);
      environment.setPending(request.getType() == RepositoryHookType.PRE_RECEIVE);

      HgHookContextProvider context = hookContextProviderFactory.create(request.getRepositoryId(), request.getNode());
      hookEventFacade.handle(request.getRepositoryId()).fireHookEvent(request.getType(), context);

      return new Response(context.getHgMessageProvider().getMessages(), false);
    } catch (AuthenticationException ex) {
      LOG.warn("hook authentication failed", ex);
      return error("hook authentication failed");
    } catch (NotFoundException ex) {
      LOG.warn("could not find repository with id {}", request.getRepositoryId(), ex);
      return error("repository not found");
    } catch (Exception ex) {
      LOG.warn("unknown error on hook occurred", ex);
      return error("unknown error");
    } finally {
      environment.clearPendingState();
    }
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
