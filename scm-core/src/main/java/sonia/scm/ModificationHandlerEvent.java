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

package sonia.scm;

import sonia.scm.event.HandlerEvent;

/**
 * Extension to the {@link ModificationHandlerEvent}.
 *
 * @param <T> type of changed item
 *
 * @since 1.48
 */
public interface ModificationHandlerEvent<T> extends HandlerEvent<T>
{
  /**
   * Returns item, before it was modified.
   */
  public T getItemBeforeModification();
}
