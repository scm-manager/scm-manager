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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.WebappConfigProvider;

/**
 * {@link RestartStrategy} which tears down the scm-manager context and
 * then exists the java process with {@link System#exit(int)}.
 * <p>
 * This is useful if an external mechanism is able to restart the process after it has exited.
 */
class ExitRestartStrategy extends RestartStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(ExitRestartStrategy.class);

  static final String NAME = "exit";


  ExitRestartStrategy() {
  }

  @Override
  public void prepareRestart(InjectionContext context) {
    // Nothing to do
  }

  @Override
  protected void executeRestart(InjectionContext context) {
    Integer exitCode = WebappConfigProvider.resolveAsInteger("restart.exitCode").orElse(0);
    LOG.warn("exit scm-manager with exit code {}", exitCode);
    System.exit(exitCode);
  }
}
