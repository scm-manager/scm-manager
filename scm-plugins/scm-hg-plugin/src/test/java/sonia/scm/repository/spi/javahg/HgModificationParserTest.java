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
