/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
    super(entity("File Lock", lock.getPath()).in(namespaceAndName).build(), "File " + lock.getPath() + " locked by " + lock.getUserId());
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
