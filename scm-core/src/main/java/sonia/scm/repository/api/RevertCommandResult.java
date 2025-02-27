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

import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;

import static java.util.Collections.emptyList;

/**
 * Contains the result of an executed revert command.
 *
 * @since 3.8
 */
@Getter
public class RevertCommandResult {

  /**
   * The identifier of the revision after the applied revert.
   */
  private final String revision;
  /**
   * A collection of files where conflicts occur.
   */
  private final Collection<String> filesWithConflict;

  /**
   * Creates a {@link RevertCommandResult}.
   *
   * @param revision          revision identifier
   * @param filesWithConflict a collection of files where conflicts occur
   */
  public RevertCommandResult(String revision, Collection<String> filesWithConflict) {
    this.revision = revision;
    this.filesWithConflict = filesWithConflict;
  }

  /**
   * Used to indicate a successful revert.
   *
   * @param newHeadRevision id of the newly created revert
   * @return {@link RevertCommandResult}
   */
  public static RevertCommandResult success(String newHeadRevision) {
    return new RevertCommandResult(newHeadRevision, emptyList());
  }

  /**
   * Used to indicate a failed revert.
   *
   * @param filesWithConflict collection of conflicting files
   * @return {@link RevertCommandResult}
   */
  public static RevertCommandResult failure(Collection<String> filesWithConflict) {
    return new RevertCommandResult(null, new HashSet<>(filesWithConflict));
  }

  /**
   * If this returns <code>true</code>, the revert was successful. If this returns <code>false</code>, there may have
   * been problems like a merge conflict after the revert.
   */
  public boolean isSuccessful() {
    return filesWithConflict.isEmpty() && revision != null;
  }
}
