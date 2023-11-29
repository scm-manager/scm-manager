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
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.metrics.Metrics;

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
  private final MeterRegistry registry;

  private ExecutorService acceptor;
  private ExecutorService workerPool;
  private ServerSocket serverSocket;
  private SecurityManager securityManager;

  @Inject
  public HookServer(HookHandlerFactory handlerFactory, MeterRegistry registry) {
    this.handlerFactory = handlerFactory;
    this.registry = registry;
  }

  public int start() throws IOException {
    securityManager = SecurityUtils.getSecurityManager();

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
    acceptor.submit(() -> {
      while (!serverSocket.isClosed()) {
        try {
          LOG.trace("wait for next hook connection");
          Socket clientSocket = serverSocket.accept();
          LOG.trace("accept incoming hook client from {}", clientSocket.getInetAddress());
          HookHandler hookHandler = handlerFactory.create(clientSocket);
          workerPool.submit(associateSecurityManager(hookHandler));
        } catch (IOException ex) {
          LOG.debug("failed to accept socket, possible closed", ex);
        }
      }
      LOG.warn("ServerSocket is closed");
    });
  }

  private Runnable associateSecurityManager(HookHandler hookHandler) {
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

  private ExecutorService createAcceptor() {
    ExecutorService executorService = Executors.newSingleThreadExecutor(
      createThreadFactory("HgHookAcceptor")
    );
    Metrics.executor(registry, executorService, "HgHookServerAcceptor", "single");
    return executorService;
  }

  private ExecutorService createWorkerPool() {
    ExecutorService executorService =  Executors.newCachedThreadPool(
      createThreadFactory("HgHookWorker-%d")
    );
    Metrics.executor(registry, executorService, "HgHookServerWorker", "cached");
    return executorService;
  }

  @Nonnull
  private ThreadFactory createThreadFactory(String hgHookAcceptor) {
    return new ThreadFactoryBuilder()
      .setNameFormat(hgHookAcceptor)
      .build();
  }

  @Override
  public void close() {
    closeSocket();
    shutdown(acceptor);
    shutdown(workerPool);
  }

  private void closeSocket() {
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException ex) {
        LOG.warn("failed to close server socket", ex);
      }
    }
  }

  private void shutdown(ExecutorService acceptor) {
    if (acceptor != null) {
      acceptor.shutdown();
    }
  }
}
