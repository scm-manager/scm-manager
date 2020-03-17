/**
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
