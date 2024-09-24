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

import java.util.Collection;

/**
 * This class exists to bypass the visibility of the {@link RepositoryReadOnlyChecker} methods.
 * This is necessary to use those methods from test which are located in other packages.
 */
public class RepositoryReadOnlyCheckerHack {

  private RepositoryReadOnlyCheckerHack() {
  }

  /**
   * Bypass the visibility of the {@link RepositoryReadOnlyChecker#setReadOnlyChecks(Collection)} method.
   *
   * @param readOnlyChecks checks to register
   */
  public static void setReadOnlyChecks(Collection<ReadOnlyCheck> readOnlyChecks) {
    RepositoryReadOnlyChecker.setReadOnlyChecks(readOnlyChecks);
  }
}
