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

import lombok.Value;
import sonia.scm.event.Event;

/**
 * Event is fired whenever the default branch of a repository was modified.
 *
 * @since 2.23.0
 */
@Event
@Value
public class DefaultBranchChangedEvent {

  Repository repository;
  String oldDefaultBranch;
  String newDefaultBranch;

}
