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

public enum MergeStrategy {
  MERGE_COMMIT(true),
  FAST_FORWARD_IF_POSSIBLE(true),
  SQUASH(true),
  REBASE(false);

  private final boolean commitMessageAllowed;

  MergeStrategy(boolean commitMessageAllowed) {
    this.commitMessageAllowed = commitMessageAllowed;
  }

  public boolean isCommitMessageAllowed() {
    return commitMessageAllowed;
  }
}
