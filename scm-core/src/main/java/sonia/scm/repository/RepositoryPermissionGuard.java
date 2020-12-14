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
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.BooleanSupplier;

import static sonia.scm.repository.EventDrivenRepositoryArchiveCheck.isRepositoryArchived;

public class RepositoryPermissionGuard implements PermissionGuard<Repository> {

  private static final Collection<String> READ_ONLY_VERBS = Collections.synchronizedSet(new HashSet<>());

  public static void setReadOnlyVerbs(Collection<String> readOnlyVerbs) {
    READ_ONLY_VERBS.addAll(readOnlyVerbs);
  }

  @Override
  public PermissionActionCheckInterceptor<Repository> intercept(String permission) {
    if (READ_ONLY_VERBS.contains(permission)) {
      return new PermissionActionCheckInterceptor<Repository>() {};
    } else {
      return new WriteInterceptor();
    }
  }

  private static class WriteInterceptor implements PermissionActionCheckInterceptor<Repository> {
    @Override
    public void check(Subject subject, String id, Runnable delegate) {
      delegate.run();
      if (isRepositoryArchived(id)) {
        throw new AuthorizationException("repository is archived");
      }
    }

    @Override
    public boolean isPermitted(Subject subject, String id, BooleanSupplier delegate) {
      return false;
    }
  }
}
