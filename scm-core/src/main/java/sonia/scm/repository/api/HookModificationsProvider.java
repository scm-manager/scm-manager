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

import sonia.scm.repository.Modifications;

/**
 * The HookDiffProvider returns modifications of branches that have been changed during the current hook.
 *
 * @since 2.48.0
 */
public interface HookModificationsProvider {
  /**
   * If the given branch has been updated during the current hook, this method returns an {@link Modifications} instance
   * with the modifications of the branch.
   * If the branch has been deleted, this will return {@link sonia.scm.repository.Removed} modifications for all paths.
   * Accordingly, if the branch has been created, this will return {@link sonia.scm.repository.Added} modifications
   * for all paths.
   */
  Modifications getModifications(String branchName);
}
