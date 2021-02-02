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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.spi.GitHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;

import javax.inject.Inject;

public class GitHookEventFacade {

  public static final Logger LOG = LoggerFactory.getLogger(GitHookEventFacade.class);

  private final HookEventFacade hookEventFacade;

  private static final ThreadLocal<GitHookContextProvider> PENDING_POST_HOOK = new ThreadLocal<>();

  @Inject
  public GitHookEventFacade(HookEventFacade hookEventFacade) {
    this.hookEventFacade = hookEventFacade;
  }

  public void fire(RepositoryHookType type, GitHookContextProvider context) {
    switch (type) {
      case PRE_RECEIVE:
        doFire(type, context);
        break;
      case POST_RECEIVE:
        Thread thread = Thread.currentThread();
        if ("JGit-Receive-Pack".equals(thread.getName())) {
          LOG.debug("handling internal git thread for post receive hook");
          handleGitInternalThread(context, thread);
        } else {
          LOG.debug("register post receive hook for repository id {} in thread {}", context.getRepositoryId(), thread);
          PENDING_POST_HOOK.set(context);
        }
        break;
    }
  }

  public void firePending() {
    try {
      LOG.debug("fire pending post receive hooks in thread {}", Thread.currentThread());
      doFire(RepositoryHookType.POST_RECEIVE, PENDING_POST_HOOK.get());
    } finally {
      clean();
    }
  }

  private void handleGitInternalThread(GitHookContextProvider context, Thread thread) {
    new Thread(() -> {
      try {
        thread.join();
      } catch (InterruptedException e) {
        LOG.debug("got interrupted in internal git thread for repository id {}", context.getRepositoryId(), e);
      } finally {
        LOG.debug("internal git thread ended for repository id {}", context.getRepositoryId());
        doFire(RepositoryHookType.POST_RECEIVE, context);
      }
    }).start();
  }

  public void clean() {
    PENDING_POST_HOOK.remove();
  }

  private void doFire(RepositoryHookType type, GitHookContextProvider context) {
    if (context != null) {
      LOG.debug("firing {} hook for repository {} in Thread {}", type, context.getRepositoryId(), Thread.currentThread());
      hookEventFacade.handle(context.getRepositoryId()).fireHookEvent(type, context);
    } else {
      LOG.debug("No context found for event type {} in Thread {}", type, Thread.currentThread());
    }
  }
}
