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


import sonia.scm.event.Event;

/**
 * This event indicates a forced restart of scm-manager.
 * @since 2.0.0
 */
@Event
public class RestartEvent {

  private final Class<?> cause;
  private final String reason;

  RestartEvent(Class<?> cause, String reason) {
    this.cause = cause;
    this.reason = reason;
  }

  /**
   * The class which has fired the restart event.
   */
  public Class<?> getCause() {
    return cause;
  }

  /**
   * Returns the reason for the restart.
   */
  public String getReason() {
    return reason;
  }

}
