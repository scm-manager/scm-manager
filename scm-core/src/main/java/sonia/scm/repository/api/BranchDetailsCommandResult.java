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

import sonia.scm.repository.BranchDetails;

/**
 * @since 2.28.0
 */
public class BranchDetailsCommandResult {
  private final BranchDetails details;

  /**
   * Creates the result object
   *
   * @param details The details for the branch
   */
  public BranchDetailsCommandResult(BranchDetails details) {
    this.details = details;
  }

  /**
   * The details for the branch.
   */
  public BranchDetails getDetails() {
    return details;
  }
}
