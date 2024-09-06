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

package sonia.scm.repository.spi;

/**
 * @since 2.4.0
 */
public interface HookMergeDetectionProvider {

  /**
   * Checks whether <code>branch</code> has been merged into <code>target</code>. So this will also return
   * <code>true</code>, when <code>branch</code> has been deleted with this change.
   *
   * @param target The name of the branch to check, whether the other branch has been merged into.
   * @param branch The name of the branch to check, whether it has been merged into the other branch.
   * @return <code>true</code> when <code>branch</code> has been merged into <code>target</code>, <code>false</code>
   * otherwise.
   */
  boolean branchesMerged(String target, String branch);
}
