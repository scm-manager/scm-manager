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

import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.spi.GitHookContextProvider;
import sonia.scm.repository.spi.HookEventFacade;

import javax.inject.Inject;

public class GitHookEventFacade {

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
          handleGitInternalThread(context, thread);
        } else {
          PENDING_POST_HOOK.set(context);
        }
        break;
    }
  }

  private void handleGitInternalThread(GitHookContextProvider context, Thread thread) {
    new Thread(() -> {
      try {
        thread.join();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        doFire(RepositoryHookType.POST_RECEIVE, context);
      }
    }).start();
  }

  public void firePending() {
    try {
      doFire(RepositoryHookType.POST_RECEIVE, PENDING_POST_HOOK.get());
    } finally {
      clean();
    }
  }

  public void clean() {
    PENDING_POST_HOOK.remove();
  }

  private void doFire(RepositoryHookType type, GitHookContextProvider context) {
    if (context != null) {
      hookEventFacade.handle(context.getRepositoryId()).fireHookEvent(type, context);
    } else {
      throw new IllegalStateException("Context not present");
    }
  }
}
