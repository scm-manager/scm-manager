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

package sonia.scm.repository.spi.javahg;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.Added;
import sonia.scm.repository.Copied;
import sonia.scm.repository.Modified;
import sonia.scm.repository.Removed;
import sonia.scm.repository.Renamed;

import static org.assertj.core.api.Assertions.assertThat;

class HgModificationParserTest {

  HgModificationParser parser = new HgModificationParser();

  @Test
  void shouldDetectAddedPath() {
    parser.addLine("a added/file");

    assertThat(parser.getModifications())
      .containsExactly(new Added("added/file"));
  }

  @Test
  void shouldDetectModifiedPath() {
    parser.addLine("m modified/file");

    assertThat(parser.getModifications())
      .containsExactly(new Modified("modified/file"));
  }

  @Test
  void shouldDetectRemovedPath() {
    parser.addLine("r removed/file");

    assertThat(parser.getModifications())
      .containsExactly(new Removed("removed/file"));
  }

  @Test
  void shouldDetectRenamedPath() {
    parser.addLine("a new/path");
    parser.addLine("r old/path");
    parser.addLine("c old/path\0new/path");

    assertThat(parser.getModifications())
      .containsExactly(new Renamed("old/path", "new/path"));
  }

  @Test
  void shouldCopiedRenamedPath() {
    parser.addLine("a new/path");
    parser.addLine("c old/path\0new/path");

    assertThat(parser.getModifications())
      .containsExactly(new Copied("old/path", "new/path"));
  }
}
