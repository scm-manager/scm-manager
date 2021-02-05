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
