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

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ScmWildcardPermissionTest {

  @Test
  void shouldEliminatePermissionsWithDifferentSubject() {
    ScmWildcardPermission permission = new ScmWildcardPermission("user:write:*");

    Optional<ScmWildcardPermission> limitedPermissions = permission.limit("repository:write:*");

    assertThat(limitedPermissions).isEmpty();
  }

  @Test
  void shouldReturnScopeIfPermissionImpliesScope() {
    ScmWildcardPermission permission = new ScmWildcardPermission("*");

    Optional<ScmWildcardPermission> limitedPermission = permission.limit("repository:read:42");

    assertThat(limitedPermission).get().hasToString("repository:read:42");
  }

  @Test
  void shouldReturnPermissionIfScopeImpliesPermission() {
    ScmWildcardPermission permission = new ScmWildcardPermission("repository:read:42");

    Optional<ScmWildcardPermission> limitedPermission = permission.limit("repository:*:42");

    assertThat(limitedPermission).get().hasToString("repository:read:42");
  }

  @Test
  void shouldLimitExplicitParts() {
    ScmWildcardPermission permission = new ScmWildcardPermission("repository:read,write:42,43,44");

    Optional<ScmWildcardPermission> limitedPermission = permission.limit("repository:read,write,pull:42");

    assertThat(limitedPermission).get().hasToString("repository:read,write:42");
  }

  @Test
  void shouldDetectWildcard() {
    ScmWildcardPermission permission = new ScmWildcardPermission("repository:read,write:*");

    Optional<ScmWildcardPermission> limitedPermission = permission.limit("repository:*:42");

    assertThat(limitedPermission).get().hasToString("repository:read,write:42");
  }

  @Test
  void shouldHandleMissingEntriesAsWildcard() {
    ScmWildcardPermission permission = new ScmWildcardPermission("repository:read,write");

    Optional<ScmWildcardPermission> limitedPermission = permission.limit("repository:*:42");

    assertThat(limitedPermission).get().hasToString("repository:read,write:42");
  }

  @Test
  void shouldEliminateEmptyVerbs() {
    ScmWildcardPermission permission = new ScmWildcardPermission("repository:read:42");

    Optional<ScmWildcardPermission> limitedPermission = permission.limit("repository:pull:42");

    assertThat(limitedPermission).isEmpty();
  }

  @Test
  void shouldEliminateEmptyId() {
    ScmWildcardPermission permission = new ScmWildcardPermission("repository:read:42");

    Optional<ScmWildcardPermission> limitedPermission = permission.limit("repository:read:23");

    assertThat(limitedPermission).isEmpty();
  }
}
