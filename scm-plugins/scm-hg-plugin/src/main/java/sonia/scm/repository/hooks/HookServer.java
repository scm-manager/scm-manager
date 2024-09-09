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
