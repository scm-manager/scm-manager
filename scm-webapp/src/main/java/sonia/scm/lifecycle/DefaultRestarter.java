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
package sonia.scm.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.event.ScmEventBus;

@Singleton
public class DefaultRestarter implements Restarter {
  private final ScmEventBus eventBus;
  private final RestartStrategy strategy;

  @Inject
  public DefaultRestarter() {
    this(
      ScmEventBus.getInstance(),
      RestartStrategy.get(Thread.currentThread().getContextClassLoader()).orElse(null)
    );
  }

  @VisibleForTesting
  DefaultRestarter(ScmEventBus eventBus, RestartStrategy strategy) {
    this.eventBus = eventBus;
    this.strategy = strategy;
  }

  @Override
  public boolean isSupported() {
    return strategy != null;
  }

  @Override
  public void restart(Class<?> cause, String reason) {
    if (!isSupported()) {
      throw new RestartNotSupportedException("restarting is not supported");
    }
    eventBus.post(new RestartEvent(cause, reason));
  }
}
