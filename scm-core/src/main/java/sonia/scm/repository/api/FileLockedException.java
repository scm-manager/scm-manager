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

import sonia.scm.ExceptionWithContext;
import sonia.scm.repository.NamespaceAndName;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

/**
 * Exception thrown whenever a locked file should be modified or locked/unlocked by a user that does not hold the lock.
 *
 * @since 2.26.0
 */
public class FileLockedException extends ExceptionWithContext {

  private static final String CODE = "3mSmwOtOd1";

  private final FileLock conflictingLock;

  /**
   * Creates the exception.
   *
   * @param namespaceAndName The namespace and name of the repository.
   * @param lock             The lock causing this exception.
   */
  public FileLockedException(NamespaceAndName namespaceAndName, FileLock lock) {
    this(namespaceAndName, lock, "");
  }

  /**
   * Creates the exception with an additional message.
   *
   * @param namespaceAndName  The namespace and name of the repository.
   * @param lock              The lock causing this exception.
   * @param additionalMessage An additional message that will be appended to the default message.
   */
  public FileLockedException(NamespaceAndName namespaceAndName, FileLock lock, String additionalMessage) {
    super(
      entity("File Lock", lock.getPath()).in(namespaceAndName).build(),
      ("File " + lock.getPath() + " locked by " + lock.getUserId() + ". " + additionalMessage).trim());
    this.conflictingLock = lock;
  }

  @Override
  public String getCode() {
    return CODE;
  }

  /**
   * The lock that caused this exception.
   */
  public FileLock getConflictingLock() {
    return conflictingLock;
  }
}
