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

import sonia.scm.event.Event;

/**
 * This event is fired when a public key was created in SCM-Manager.
 * @since 2.4.0
 */
@Event
public final class PublicKeyCreatedEvent {
  private final PublicKey key;

  public PublicKeyCreatedEvent(PublicKey key) {
    this.key = key;
  }

  public PublicKey getKey() {
    return key;
  }
}
