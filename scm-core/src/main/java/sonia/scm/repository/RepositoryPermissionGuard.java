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
