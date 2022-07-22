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

package sonia.scm.repository;

import java.util.Collection;
import java.util.Optional;

public interface RepositoryPermissionHolder {

  Collection<RepositoryPermission> getPermissions();

  void setPermissions(Collection<RepositoryPermission> permissions);

  void addPermission(RepositoryPermission newPermission);

  boolean removePermission(RepositoryPermission permission);

  /**
   * Returns the permission for the given user, if present, or an empty {@link Optional} otherwise.
   *
   * @since 2.38.0
   */
  default Optional<RepositoryPermission> findUserPermission(String userId) {
    return findPermission(userId, false);
  }

  /**
   * Returns the permission for the given group, if present, or an empty {@link Optional} otherwise.
   *
   * @since 2.38.0
   */
  default Optional<RepositoryPermission> findGroupPermission(String groupId) {
    return findPermission(groupId, true);
  }

  private Optional<RepositoryPermission> findPermission(String x, boolean isGroup) {
    return getPermissions().stream().filter(p -> p.isGroupPermission() == isGroup && p.getName().equals(x)).findFirst();
  }
}
