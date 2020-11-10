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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Singleton
public class HookServer implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(HookServer.class);

  private final HookHandlerFactory handlerFactory;

  private ExecutorService acceptor;
  private ExecutorService workerPool;
  private ServerSocket serverSocket;

  @Inject
  public HookServer(HookHandlerFactory handlerFactory) {
    this.handlerFactory = handlerFactory;
  }

  private ExecutorService createAcceptor() {
    return Executors.newSingleThreadExecutor(
      createThreadFactory("HgHookAcceptor")
    );
  }

  private ExecutorService createWorkerPool() {
    return Executors.newCachedThreadPool(
      createThreadFactory("HgHookWorker-%d")
    );
  }

  @Nonnull
  private ThreadFactory createThreadFactory(String hgHookAcceptor) {
    return new ThreadFactoryBuilder()
      .setNameFormat(hgHookAcceptor)
      .build();
  }

  public int start() throws IOException {
    acceptor = createAcceptor();
    workerPool = createWorkerPool();
    serverSocket = createServerSocket();
    // set timeout to 2 min, to avoid blocking clients
    serverSocket.setSoTimeout(2 * 60 * 1000);

    accept();

    int port = serverSocket.getLocalPort();
    LOG.info("open hg hook server on port {}", port);
    return port;
  }

  private void accept() {
    SecurityManager securityManager = SecurityUtils.getSecurityManager();
    acceptor.submit(() -> {
      while (!serverSocket.isClosed()) {
        try {
          LOG.trace("wait for next hook connection");
          Socket clientSocket = serverSocket.accept();
          LOG.trace("accept incoming hook client from {}", clientSocket.getInetAddress());
          HookHandler hookHandler = handlerFactory.create(clientSocket);
          workerPool.submit(associateSecurityManager(securityManager, hookHandler));
        } catch (IOException ex) {
          LOG.debug("failed to accept socket, possible closed", ex);
        }
      }
      LOG.warn("ServerSocket is closed");
    });
  }

  private Runnable associateSecurityManager(SecurityManager securityManager, HookHandler hookHandler) {
    return () -> {
      ThreadContext.bind(securityManager);
      try {
        hookHandler.run();
      } finally {
        ThreadContext.unbindSubject();
        ThreadContext.unbindSecurityManager();
      }
    };
  }

  @Nonnull
  private ServerSocket createServerSocket() throws IOException {
    return new ServerSocket(0, 0, InetAddress.getLoopbackAddress());
  }

  @Override
  public void close() throws IOException {
    if (serverSocket != null) {
      serverSocket.close();
    }
    shutdown(acceptor);
    shutdown(workerPool);
  }

  private void shutdown(ExecutorService acceptor) {
    if (acceptor != null) {
      acceptor.shutdown();
    }
  }
}
