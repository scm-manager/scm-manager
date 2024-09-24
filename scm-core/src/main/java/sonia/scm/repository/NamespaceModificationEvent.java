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

package sonia.scm.repository;

import sonia.scm.HandlerEventType;
import sonia.scm.ModificationHandlerEvent;
import sonia.scm.event.Event;

/**
 * Event which is fired whenever a namespace is modified.
 *
 * @since 2.6.0
 */
@Event
public final class NamespaceModificationEvent extends NamespaceEvent implements ModificationHandlerEvent<Namespace> {

  private final Namespace itemBeforeModification;

  public NamespaceModificationEvent(HandlerEventType eventType, Namespace item, Namespace itemBeforeModification) {
    super(eventType, item, itemBeforeModification);
    this.itemBeforeModification = itemBeforeModification;
  }

  @Override
  public Namespace getItemBeforeModification() {
    return itemBeforeModification;
  }

}
