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

package sonia.scm.importexport;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.RepositoryPermission;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryImportPermissionMergerTest {

  private final RepositoryImportPermissionMerger merger = new RepositoryImportPermissionMerger();

  @Test
  void shouldReturnExistingPermissionsIfNoImportedPermissions() {
    RepositoryPermission owner = new RepositoryPermission("owner", ImmutableSet.of("*"), false);
    Collection<RepositoryPermission> existing = ImmutableSet.of(owner);
    Collection<RepositoryPermission> imported = Collections.emptySet();

    Collection<RepositoryPermission> result = merger.merge(existing, imported);

    assertThat(result)
      .hasSize(1)
      .contains(owner);
  }

  @Test
  void shouldReturnMergedPermissions() {
    RepositoryPermission owner = new RepositoryPermission("owner", ImmutableSet.of("*"), false);
    RepositoryPermission trillian = new RepositoryPermission("trillian", ImmutableSet.of("read"), false);
    Collection<RepositoryPermission> existing = ImmutableSet.of(owner);
    Collection<RepositoryPermission> imported = ImmutableSet.of(trillian);

    Collection<RepositoryPermission> result = merger.merge(existing, imported);

    assertThat(result)
      .hasSize(2)
      .contains(owner)
      .contains(trillian);
  }

  @Test
  void shouldReturnOnlyMergePermissionIfNotExistYet() {
    RepositoryPermission owner = new RepositoryPermission("owner", ImmutableSet.of("*"), false);
    RepositoryPermission trillian = new RepositoryPermission("trillian", ImmutableSet.of("read", "write"), false);
    RepositoryPermission importedOwner = new RepositoryPermission("owner", ImmutableSet.of("read, write"), false);
    RepositoryPermission importedTrillian = new RepositoryPermission("trillian", ImmutableSet.of("read"), false);

    Collection<RepositoryPermission> existing = ImmutableSet.of(owner, trillian);
    Collection<RepositoryPermission> imported = ImmutableSet.of(importedOwner, importedTrillian);

    Collection<RepositoryPermission> result = merger.merge(existing, imported);

    assertThat(result)
      .hasSize(2)
      .contains(owner)
      .contains(trillian)
      .doesNotContain(importedOwner)
      .doesNotContain(importedTrillian);
  }

  @Test
  void shouldAddPermissionWithSameNameIfOneIsGroupPermission() {
    RepositoryPermission owner = new RepositoryPermission("owner", ImmutableSet.of("*"), false);
    RepositoryPermission trillian = new RepositoryPermission("trillian", ImmutableSet.of("read", "write"), false);
    RepositoryPermission importedOwner = new RepositoryPermission("owner", ImmutableSet.of("read, write"), true);
    RepositoryPermission importedTrillian = new RepositoryPermission("trillian", ImmutableSet.of("read"), true);

    Collection<RepositoryPermission> existing = ImmutableSet.of(owner, trillian);
    Collection<RepositoryPermission> imported = ImmutableSet.of(importedOwner, importedTrillian);

    Collection<RepositoryPermission> result = merger.merge(existing, imported);

    assertThat(result)
      .hasSize(4)
      .contains(owner)
      .contains(trillian)
      .contains(importedOwner)
      .contains(importedTrillian);
  }

  @Test
  void shouldNotAddPermissionMultipleTimes() {
    RepositoryPermission owner = new RepositoryPermission("owner", ImmutableSet.of("*"), false);
    RepositoryPermission importedTrillian1 = new RepositoryPermission("trillian", ImmutableSet.of("read, write"), false);
    RepositoryPermission importedTrillian2 = new RepositoryPermission("trillian", ImmutableSet.of("read"), false);

    Collection<RepositoryPermission> existing = ImmutableSet.of(owner);
    Collection<RepositoryPermission> imported = ImmutableSet.of(importedTrillian1, importedTrillian2);

    Collection<RepositoryPermission> result = merger.merge(existing, imported);

    assertThat(result)
      .hasSize(2)
      .contains(owner)
      .contains(importedTrillian1)
      .doesNotContain(importedTrillian2);
  }
}
