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
