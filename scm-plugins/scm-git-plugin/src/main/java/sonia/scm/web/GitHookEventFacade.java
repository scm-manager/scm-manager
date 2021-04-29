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

package sonia.scm.web;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.metrics.Metrics;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.spi.GitHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is used to delay the firing of post commit hooks, so that they are not
 * executed while the internal git processing has not finished. Without this, this can
 * lead to conflicting access to pack files which results in 'Short compressed stream'
 * errors (see https://github.com/scm-manager/scm-manager/pull/1518).
 * <br>
 * The delay is handled either by "caching" the hook data in a thread local, where it
 * is fetched from when {@link #firePending(GitHookContextProvider)} is called, or in case the trigger is fired
 * due to changes made with internal git file push (from a workdir to the central
 * repository) by detecting the internal thread used by JGit and joining another thread
 * where the pending push is triggered.
 */
public class GitHookEventFacade implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(GitHookEventFacade.class);

  private final HookEventFacade hookEventFacade;
  private final ExecutorService internalThreadHookHandler;

  @Inject
  public GitHookEventFacade(HookEventFacade hookEventFacade, MeterRegistry registry) {
    this.hookEventFacade = hookEventFacade;
    this.internalThreadHookHandler = createInternalThreadHookHandlerPool(registry);
  }

  public void fire(RepositoryHookType type, GitHookContextProvider context) {
    switch (type) {
      case PRE_RECEIVE:
        doFire(type, context);
        break;
      case POST_RECEIVE:
        break;
      default:
        throw new IllegalArgumentException("unknown hook type: " + type);
    }
  }

  public void firePending(GitHookContextProvider context) {
    LOG.debug("fire pending post receive hooks in thread {}", Thread.currentThread());
    doFire(RepositoryHookType.POST_RECEIVE, context);
  }

  private void doFire(RepositoryHookType type, GitHookContextProvider context) {
    if (context != null) {
      LOG.debug("firing {} hook for repository {} in Thread {}", type, context.getRepositoryId(), Thread.currentThread());
      hookEventFacade.handle(context.getRepositoryId()).fireHookEvent(type, context);
    } else {
      LOG.debug("No context found for event type {} in Thread {}", type, Thread.currentThread());
    }
  }

  @Nonnull
  private ExecutorService createInternalThreadHookHandlerPool(MeterRegistry registry) {
    ExecutorService executorService = Executors.newCachedThreadPool(
      new ThreadFactoryBuilder()
        .setNameFormat("GitInternalThreadHookHandler-%d")
        .build()
    );
    Metrics.executor(registry, executorService, "GitInternalHookHandler", "cached");
    return executorService;
  }

  @Override
  public void close() {
    internalThreadHookHandler.shutdown();
  }
}
