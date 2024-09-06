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

package sonia.scm.repository.api;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

class HunkTest {

  @Test
  void shouldGetComplexHeader() {
    String rawHeader = createHunk(2, 3, 4, 5).getRawHeader();

    assertThat(rawHeader).isEqualTo("@@ -2,3 +4,5 @@");
  }

  @Test
  void shouldReturnSingleNumberForOne() {
    String rawHeader = createHunk(42, 1, 5, 1).getRawHeader();

    assertThat(rawHeader).isEqualTo("@@ -42 +5 @@");
  }

  private Hunk createHunk(int oldStart, int oldLineCount, int newStart, int newLineCount) {
    return new Hunk() {
      @Override
      public int getOldStart() {
        return oldStart;
      }

      @Override
      public int getOldLineCount() {
        return oldLineCount;
      }

      @Override
      public int getNewStart() {
        return newStart;
      }

      @Override
      public int getNewLineCount() {
        return newLineCount;
      }

      @Override
      public Iterator<DiffLine> iterator() {
        return null;
      }
    };
  }
}
