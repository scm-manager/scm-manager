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

package sonia.scm.repository.api;

/**
 * Send messages back to scm client during hook execution.
 *
 * @since 1.33
 */
public interface HookMessageProvider
{

  /**
   * Send error message back to scm client.
   *
   * @param message error message to send
   */
  public void sendError(String message);

  /**
   * Send message back tp scm client. If the message is a notification about an
   * error, please use {@link #sendError(String)} instead.
   *
   * @param message error message to send
   */
  public void sendMessage(String message);
}
