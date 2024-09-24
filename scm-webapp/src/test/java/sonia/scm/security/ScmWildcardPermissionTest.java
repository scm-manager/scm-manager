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
