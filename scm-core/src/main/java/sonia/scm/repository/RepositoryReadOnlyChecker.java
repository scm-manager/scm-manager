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

import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Checks, whether a repository has to be considered read only. Currently, this includes {@link RepositoryArchivedCheck}
 * and {@link RepositoryExportingCheck}.
 *
 * @since 2.14.0
 */
public final class RepositoryReadOnlyChecker {

  private static Set<ReadOnlyCheck> staticChecks = Collections.emptySet();

  /**
   * Set static read only checks.
   *
   * @param readOnlyChecks static read only checks
   */
  static void setReadOnlyChecks(Collection<ReadOnlyCheck> readOnlyChecks) {
    staticChecks = ImmutableSet.copyOf(readOnlyChecks);
  }

  /**
   * We should use {@link #staticChecks} instead of checks.
   * Checks exists only for backward compatibility.
   */
  private final Set<ReadOnlyCheck> checks = new HashSet<>();

  /**
   * Constructs a new read only checker, which uses only static checks.
   */
  @Inject
  public RepositoryReadOnlyChecker() {
  }

  /**
   * Constructs a new read only checker.
   *
   * @deprecated use {@link RepositoryReadOnlyChecker#setReadOnlyChecks(Collection)} instead
   */
  @Deprecated
  public RepositoryReadOnlyChecker(RepositoryArchivedCheck archivedCheck, RepositoryExportingCheck exportingCheck) {
    this.checks.addAll(Arrays.asList(archivedCheck, exportingCheck));
  }

  /**
   * Checks if the repository is read only.
   * @param repository The repository to check.
   * @return <code>true</code> if any check locks the repository to read only access.
   */
  public boolean isReadOnly(Repository repository) {
    return isReadOnly(repository.getId());
  }

  /**
   * Checks if the repository for the given id is read only.
   * @param repositoryId The id of the given repository to check.
   * @return <code>true</code> if any check locks the repository to read only access.
   */
  public boolean isReadOnly(String repositoryId) {
    return Stream.concat(checks.stream(), staticChecks.stream()).anyMatch(check -> check.isReadOnly(repositoryId));
  }

  /**
   * Checks if the repository may be modified.
   *
   * @throws ReadOnlyException if the repository is marked as read only
   */
  public static void checkReadOnly(Repository repository) {
    staticChecks.forEach(check -> check.check(repository));
  }
}
