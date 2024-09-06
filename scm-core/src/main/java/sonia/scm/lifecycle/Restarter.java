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

/**
 * {@link Restarter} is able to restart scm-manager.
 *
 * @since 2.0.0
 */
public interface Restarter {

  /**
   * Return {@code true} if restarting scm-manager is supported.
   */
  boolean isSupported();

  /**
   * Issues a restart. The method will fire a {@link RestartEvent} to notify the system about the upcoming restart.
   * If restarting is not supported by the underlying platform a {@link RestartNotSupportedException} is thrown.
   *
   * @param cause cause of the restart. This should be the class which calls this method.
   * @param reason reason for the required restart.
   * @throws RestartNotSupportedException if restarting is not supported by the underlying platform.
   */
  void restart(Class<?> cause, String reason);
}
