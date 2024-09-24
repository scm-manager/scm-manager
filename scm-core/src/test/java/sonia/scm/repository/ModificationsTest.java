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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModificationsTest {

  public static final Modifications MODIFICATIONS = new Modifications("123",
    new Added("added"),
    new Removed("removed"),
    new Modified("modified"),
    new Renamed("rename from", "rename to"),
    new Copied("copy from", "copy to")
  );

  @Test
  void shouldFindAddedFilesAsEffected() {
    assertThat(MODIFICATIONS.getEffectedPaths())
      .contains("added");
  }

  @Test
  void shouldFindRemovedFilesAsEffected() {
    assertThat(MODIFICATIONS.getEffectedPaths())
      .contains("removed");
  }

  @Test
  void shouldFindModifiedFilesAsEffected() {
    assertThat(MODIFICATIONS.getEffectedPaths())
      .contains("modified");
  }

  @Test
  void shouldFindRenamedFilesAsEffected() {
    assertThat(MODIFICATIONS.getEffectedPaths())
      .contains("rename from", "rename to");
  }

  @Test
  void shouldFindTargetOfCopiedFilesAsEffected() {
    assertThat(MODIFICATIONS.getEffectedPaths())
      .contains("copy to")
      .doesNotContain("copy from");
  }
}
