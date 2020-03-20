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
    
package sonia.scm.repository.spi;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.api.DiffLine;
import sonia.scm.repository.api.Hunk;

import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GitHunkParserTest {

  private static final String DIFF_001 = "diff --git a/a.txt b/a.txt\n" +
    "index 7898192..2f8bc28 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n" +
    "@@ -1 +1,2 @@\n" +
    " a\n" +
    "+added line\n";

  private static final String DIFF_002 = "diff --git a/file b/file\n" +
    "index 5e89957..e8823e1 100644\n" +
    "--- a/file\n" +
    "+++ b/file\n" +
    "@@ -2,6 +2,9 @@\n" +
    " 2\n" +
    " 3\n" +
    " 4\n" +
    "+5\n" +
    "+6\n" +
    "+7\n" +
    " 8\n" +
    " 9\n" +
    " 10\n" +
    "@@ -15,14 +18,13 @@\n" +
    " 18\n" +
    " 19\n" +
    " 20\n" +
    "+21\n" +
    "+22\n" +
    " 23\n" +
    " 24\n" +
    " 25\n" +
    " 26\n" +
    " 27\n" +
    "-a\n" +
    "-b\n" +
    "-c\n" +
    " 28\n" +
    " 29\n" +
    " 30";

  private static final String DIFF_003 = "diff --git a/a.txt b/a.txt\n" +
    "index 7898192..2f8bc28 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n" +
    "@@ -1,2 +1 @@\n" +
    " a\n" +
    "-removed line\n";

  private static final String ILLEGAL_DIFF = "diff --git a/a.txt b/a.txt\n" +
    "index 7898192..2f8bc28 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n" +
    "@@ -1,2 +1 @@\n" +
    " a\n" +
    "~illegal line\n";

  private static final String NO_NEWLINE_DIFF = "diff --git a/.editorconfig b/.editorconfig\n" +
    "index ea2a3ba..2f02f32 100644\n" +
    "--- a/.editorconfig\n" +
    "+++ b/.editorconfig\n" +
    "@@ -10,3 +10,4 @@\n" +
    " indent_style = space\n" +
    " indent_size = 2\n" +
    " charset = utf-8\n" +
    "+added line\n" +
    "\\ No newline at end of file\n";

  @Test
  void shouldParseHunks() {
    List<Hunk> hunks = new GitHunkParser().parse(DIFF_001);
    assertThat(hunks).hasSize(1);
    assertHunk(hunks.get(0), 1, 1, 1, 2);
  }

  @Test
  void shouldParseMultipleHunks() {
    List<Hunk> hunks = new GitHunkParser().parse(DIFF_002);

    assertThat(hunks).hasSize(2);
    assertHunk(hunks.get(0), 2, 6, 2, 9);
    assertHunk(hunks.get(1), 15, 14, 18, 13);
  }

  @Test
  void shouldParseAddedHunkLines() {
    List<Hunk> hunks = new GitHunkParser().parse(DIFF_001);

    Hunk hunk = hunks.get(0);

    Iterator<DiffLine> lines = hunk.iterator();

    DiffLine line1 = lines.next();
    assertThat(line1.getOldLineNumber()).hasValue(1);
    assertThat(line1.getNewLineNumber()).hasValue(1);
    assertThat(line1.getContent()).isEqualTo("a");

    DiffLine line2 = lines.next();
    assertThat(line2.getOldLineNumber()).isEmpty();
    assertThat(line2.getNewLineNumber()).hasValue(2);
    assertThat(line2.getContent()).isEqualTo("added line");
  }

  @Test
  void shouldParseRemovedHunkLines() {
    List<Hunk> hunks = new GitHunkParser().parse(DIFF_003);

    Hunk hunk = hunks.get(0);

    Iterator<DiffLine> lines = hunk.iterator();

    DiffLine line1 = lines.next();
    assertThat(line1.getOldLineNumber()).hasValue(1);
    assertThat(line1.getNewLineNumber()).hasValue(1);
    assertThat(line1.getContent()).isEqualTo("a");

    DiffLine line2 = lines.next();
    assertThat(line2.getOldLineNumber()).hasValue(2);
    assertThat(line2.getNewLineNumber()).isEmpty();
    assertThat(line2.getContent()).isEqualTo("removed line");
  }

  @Test
  void shouldFailForIllegalLine() {
    assertThrows(IllegalStateException.class, () -> new GitHunkParser().parse(ILLEGAL_DIFF));
  }

  @Test
  void shouldIgnoreNoNewlineLine() {
    List<Hunk> hunks = new GitHunkParser().parse(NO_NEWLINE_DIFF);

    Hunk hunk = hunks.get(0);

    Iterator<DiffLine> lines = hunk.iterator();

    DiffLine line1 = lines.next();
    assertThat(line1.getOldLineNumber()).hasValue(10);
    assertThat(line1.getNewLineNumber()).hasValue(10);
    assertThat(line1.getContent()).isEqualTo("indent_style = space");

    lines.next();
    lines.next();
    DiffLine lastLine = lines.next();
    assertThat(lastLine.getOldLineNumber()).isEmpty();
    assertThat(lastLine.getNewLineNumber()).hasValue(13);
    assertThat(lastLine.getContent()).isEqualTo("added line");
  }

  private void assertHunk(Hunk hunk, int oldStart, int oldLineCount, int newStart, int newLineCount) {
    assertThat(hunk.getOldStart()).isEqualTo(oldStart);
    assertThat(hunk.getOldLineCount()).isEqualTo(oldLineCount);

    assertThat(hunk.getNewStart()).isEqualTo(newStart);
    assertThat(hunk.getNewLineCount()).isEqualTo(newLineCount);
  }

}
