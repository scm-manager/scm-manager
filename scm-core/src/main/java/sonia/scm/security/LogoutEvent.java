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

package sonia.scm.security;

import lombok.Value;
import sonia.scm.event.Event;

/**
 * Event is fired whenever a user explicitly logs out.
 * The event is not fired if a session expires or a token is blacklisted,
 * the event is only fired if the user calls the rest method for the logout.
 *
 * @since 2.24.0
 */
@Value
@Event
public class LogoutEvent {
  String primaryPrincipal;
}
