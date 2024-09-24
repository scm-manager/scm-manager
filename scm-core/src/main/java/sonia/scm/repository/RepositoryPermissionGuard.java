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

import com.github.sdorra.ssp.PermissionActionCheckInterceptor;
import com.github.sdorra.ssp.PermissionGuard;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.BooleanSupplier;

/**
 * This intercepts permission checks for repositories and blocks write permissions for archived repositories.
 * Read only permissions are set at startup by {@link ReadOnlyCheckInitializer}.
 */
public class RepositoryPermissionGuard implements PermissionGuard<Repository> {

  private static final Collection<String> READ_ONLY_VERBS = Collections.synchronizedSet(new HashSet<>());
  private static Collection<ReadOnlyCheck> readOnlyChecks = Collections.emptySet();

  static void setReadOnlyVerbs(Collection<String> readOnlyVerbs) {
    READ_ONLY_VERBS.addAll(readOnlyVerbs);
  }

  /**
   * Sets static read only checks.
   * @param readOnlyChecks read only checks
   * @since 2.19.0
   */
  static void setReadOnlyChecks(Collection<ReadOnlyCheck> readOnlyChecks) {
    RepositoryPermissionGuard.readOnlyChecks = ImmutableSet.copyOf(readOnlyChecks);
  }

  @Override
  public PermissionActionCheckInterceptor<Repository> intercept(String permission) {
    if (READ_ONLY_VERBS.contains(permission)) {
      return new PermissionActionCheckInterceptor<Repository>() {};
    } else {
      return new WriteInterceptor(permission);
    }
  }

  private static class WriteInterceptor implements PermissionActionCheckInterceptor<Repository> {

    private final String permission;

    private WriteInterceptor(String permission) {
      this.permission = permission;
    }

    @Override
    public void check(Subject subject, String id, Runnable delegate) {
      delegate.run();
      for (ReadOnlyCheck check : readOnlyChecks) {
        if (check.isReadOnly(permission, id)) {
          throw new AuthorizationException(check.getReason());
        }
      }
    }

    @Override
    public boolean isPermitted(Subject subject, String id, BooleanSupplier delegate) {
      return isWritable(id) && delegate.getAsBoolean();
    }

    private boolean isWritable(String id) {
      return readOnlyChecks.stream().noneMatch(c -> c.isReadOnly(permission, id));
    }
  }
}
