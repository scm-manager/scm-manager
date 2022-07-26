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

/**
 * This abstracts the permissions of {@link Repository} and {@link Namespace} objects.
 */
public interface RepositoryPermissionHolder {

  /**
   * Returns a collection of all permissions for this object.
   */
  Collection<RepositoryPermission> getPermissions();

  /**
   * Sets and therefore overwrites the permissions for this object.
   *
   * @param permissions The new permissions for this object.
   */
  void setPermissions(Collection<RepositoryPermission> permissions);

  /**
   * Adds a single permission to the current set of permissions for this object.
   *
   * @param newPermission The new permission that will be added to the existing permissions.
   */
  void addPermission(RepositoryPermission newPermission);

  /**
   * Removes a single permission from the current set of permissions for this object.
   *
   * @param permission The permission that should be removed from the existing permissions.
   * @return <code>true</code>, if the given permission was part of the permissions for this object, <code>false</code>
   * otherwise.
   */
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

  private Optional<RepositoryPermission> findPermission(String name, boolean isGroup) {
    return getPermissions().stream().filter(p -> p.isGroupPermission() == isGroup && p.getName().equals(name)).findFirst();
  }
}
