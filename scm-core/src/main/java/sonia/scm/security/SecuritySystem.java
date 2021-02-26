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

package sonia.scm.security;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * The SecuritySystem manages global permissions.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public interface SecuritySystem {

  /**
   * Store a new permission.
   *
   * @param permission permission to be stored
   */
  void addPermission(AssignedPermission permission);

  /**
   * Delete stored permission.
   *
   * @param permission permission to be deleted
   */
  void deletePermission(AssignedPermission permission);

  /**
   * Return all available permissions.
   *
   * @return available permissions
   */
  Collection<PermissionDescriptor> getAvailablePermissions();

  /**
   * Returns all stored permissions which are matched by the given
   * {@link Predicate}.
   *
   * @param predicate predicate to filter
   * @return filtered permissions
   */
  Collection<AssignedPermission> getPermissions(Predicate<AssignedPermission> predicate);
}
