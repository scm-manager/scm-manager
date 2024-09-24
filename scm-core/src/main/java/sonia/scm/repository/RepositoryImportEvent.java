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

import lombok.Getter;
import sonia.scm.event.Event;

/**
 * Event which is fired whenever repository import is successful or failed.
 *
 * @since 2.11.0
 */
@Event
@Getter
public class RepositoryImportEvent {

  private final Repository item;
  private final String logId;
  private final boolean failed;

  public RepositoryImportEvent(Repository item, boolean failed) {
    this.item = item;
    this.logId = item.getId();
    this.failed = failed;
  }
}
