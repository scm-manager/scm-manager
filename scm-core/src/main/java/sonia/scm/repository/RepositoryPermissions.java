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

import com.github.sdorra.ssp.PermissionActionCheck;
import com.github.sdorra.ssp.PermissionCheck;

import java.util.Collection;
import java.util.HashSet;

/**
 * Permission checks for repository related permissions.
 */
public final class RepositoryPermissions {

  private static final Collection<String> ARCHIVED_REPOSITORIES = new HashSet<>();

  private RepositoryPermissions(){}
  /**
   * Returns permission check for create action.
   *
   * @return permission check for create action
   */
  public static PermissionCheck create() {
    return RepositoryAccessPermissions.create();
  }

  /**
   * Returns permission check for read action.
   *
   * @param id id of permission object
   *
   * @return permission check for read action
   */
  public static PermissionCheck read(String id) {
    return RepositoryAccessPermissions.read(id);
  }

  /**
   * Returns permission check for read action.
   *
   * @param item permission object
   *
   * @return permission check for read action
   */
  public static PermissionCheck read(Repository item) {
    return RepositoryAccessPermissions.read(item.getId());
  }

  /**
   * Returns permission action check for read action.
   *
   * @return permission action check for read action
   */
  public static PermissionActionCheck<RepositoryAccess> read() {
    return RepositoryAccessPermissions.read();
  }

  /**
   * Returns permission check for modify action.
   *
   * @param id id of permission object
   *
   * @return permission check for modify action
   */
  public static PermissionCheck modify(String id) {
    // TODO
    return RepositoryAccessPermissions.modify(id);
  }

  /**
   * Returns permission check for modify action.
   *
   * @param item permission object
   *
   * @return permission check for modify action
   */
  public static PermissionCheck modify(Repository item) {
    // TODO
    return RepositoryAccessPermissions.modify(item);
  }

  /**
   * Returns permission action check for modify action.
   *
   * @return permission action check for modify action
   */
  public static PermissionActionCheck<RepositoryAccess> modify() {
    return RepositoryAccessPermissions.modify();
  }

  /**
   * Returns permission check for delete action.
   *
   * @param id id of permission object
   *
   * @return permission check for delete action
   */
  public static PermissionCheck delete(String id) {
    return RepositoryAccessPermissions.delete(id);
  }

  /**
   * Returns permission check for delete action.
   *
   * @param item permission object
   *
   * @return permission check for delete action
   */
  public static PermissionCheck delete(Repository item) {
    return RepositoryAccessPermissions.delete(item);
  }

  /**
   * Returns permission action check for delete action.
   *
   * @return permission action check for delete action
   */
  public static PermissionActionCheck<RepositoryAccess> delete() {
    return RepositoryAccessPermissions.delete();
  }

  /**
   * Returns permission check for rename action.
   *
   * @param id id of permission object
   *
   * @return permission check for rename action
   */
  public static PermissionCheck rename(String id) {
    return RepositoryAccessPermissions.rename(id);
  }

  /**
   * Returns permission check for rename action.
   *
   * @param item permission object
   *
   * @return permission check for rename action
   */
  public static PermissionCheck rename(Repository item) {
    return RepositoryAccessPermissions.rename(item);
  }

  /**
   * Returns permission action check for rename action.
   *
   * @return permission action check for rename action
   */
  public static PermissionActionCheck<RepositoryAccess> rename() {
    return RepositoryAccessPermissions.rename();
  }

  /**
   * Returns permission check for healthCheck action.
   *
   * @param id id of permission object
   *
   * @return permission check for healthCheck action
   */
  public static PermissionCheck healthCheck(String id) {
    return RepositoryAccessPermissions.healthCheck(id);
  }

  /**
   * Returns permission check for healthCheck action.
   *
   * @param item permission object
   *
   * @return permission check for healthCheck action
   */
  public static PermissionCheck healthCheck(Repository item) {
    return RepositoryAccessPermissions.healthCheck(item);
  }

  /**
   * Returns permission action check for healthCheck action.
   *
   * @return permission action check for healthCheck action
   */
  public static PermissionActionCheck<RepositoryAccess> healthCheck() {
    return RepositoryAccessPermissions.healthCheck();
  }

  /**
   * Returns permission check for pull action.
   *
   * @param id id of permission object
   *
   * @return permission check for pull action
   */
  public static PermissionCheck pull(String id) {
    return RepositoryAccessPermissions.pull(id);
  }

  /**
   * Returns permission check for pull action.
   *
   * @param item permission object
   *
   * @return permission check for pull action
   */
  public static PermissionCheck pull(Repository item) {
    return RepositoryAccessPermissions.pull(item);
  }

  /**
   * Returns permission action check for pull action.
   *
   * @return permission action check for pull action
   */
  public static PermissionActionCheck<RepositoryAccess> pull() {
    return RepositoryAccessPermissions.pull();
  }

  /**
   * Returns permission check for push action.
   *
   * @param id id of permission object
   *
   * @return permission check for push action
   */
  public static PermissionCheck push(String id) {
    // TODO
    return RepositoryAccessPermissions.push(id);
  }

  /**
   * Returns permission check for push action.
   *
   * @param item permission object
   *
   * @return permission check for push action
   */
  public static PermissionCheck push(Repository item) {
    // TODO
    return RepositoryAccessPermissions.push(item);
  }

  /**
   * Returns permission action check for push action.
   *
   * @return permission action check for push action
   */
  public static PermissionActionCheck<RepositoryAccess> push() {
    return RepositoryAccessPermissions.push();
  }

  /**
   * Returns permission check for permissionRead action.
   *
   * @param id id of permission object
   *
   * @return permission check for permissionRead action
   */
  public static PermissionCheck permissionRead(String id) {
    return RepositoryAccessPermissions.permissionRead(id);
  }

  /**
   * Returns permission check for permissionRead action.
   *
   * @param item permission object
   *
   * @return permission check for permissionRead action
   */
  public static PermissionCheck permissionRead(Repository item) {
    return RepositoryAccessPermissions.permissionRead(item);
  }

  /**
   * Returns permission action check for permissionRead action.
   *
   * @return permission action check for permissionRead action
   */
  public static PermissionActionCheck<RepositoryAccess> permissionRead() {
    return RepositoryAccessPermissions.permissionRead();
  }

  /**
   * Returns permission check for permissionWrite action.
   *
   * @param id id of permission object
   *
   * @return permission check for permissionWrite action
   */
  public static PermissionCheck permissionWrite(String id) {
    // TODO
    return RepositoryAccessPermissions.permissionWrite(id);
  }

  /**
   * Returns permission check for permissionWrite action.
   *
   * @param item permission object
   *
   * @return permission check for permissionWrite action
   */
  public static PermissionCheck permissionWrite(Repository item) {
    // TODO
    return RepositoryAccessPermissions.permissionWrite(item);
  }

  /**
   * Returns permission action check for permissionWrite action.
   *
   * @return permission action check for permissionWrite action
   */
  public static PermissionActionCheck<RepositoryAccess> permissionWrite() {
    return RepositoryAccessPermissions.permissionWrite();
  }

  /**
   * Returns permission check for a custom global action.
   *
   * @return permission check for the given custom global action
   */
   public static PermissionCheck custom(String customAction) {
     // TODO
     return RepositoryAccessPermissions.custom(customAction);
   }

  /**
   * Returns permission check for custom action.
   *
   * @param customAction name of custom action
   * @param id id of permission object
   *
   * @return permission check for a custom action
   */
   public static PermissionCheck custom(String customAction, String id) {
     // TODO
     return RepositoryAccessPermissions.custom(customAction, id);
   }

  /**
   * Returns permission check for custom action.
   *
   * @param customAction name of custom action
   * @param item permission object
   *
   * @return permission check for custom action
   */
   public static PermissionCheck custom(String customAction, Repository item) {
     // TODO
     return RepositoryAccessPermissions.custom(customAction, item);
   }

  /**
   * Returns permission action check for custom action.
   *
   * @param customAction name of custom action
   *
   * @return permission action check for custom action
   */
   public static PermissionActionCheck<RepositoryAccess> customActionCheck(String customAction) {
     // TODO
     return RepositoryAccessPermissions.customActionCheck(customAction);
   }
}
