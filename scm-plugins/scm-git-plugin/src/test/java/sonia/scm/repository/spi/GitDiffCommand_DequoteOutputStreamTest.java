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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GitDiffCommand_DequoteOutputStreamTest {

  @Test
  void shouldDequoteText() throws IOException {
    String s = "diff --git \"a/file \\303\\272\\303\\274\\303\\276\\303\\253\\303\\251\\303\\245\\303\\253\\303\\245\\303\\251 a\" \"b/file \\303\\272\\303\\274\\303\\276\\303\\253\\303\\251\\303\\245\\303\\253\\303\\245\\303\\251 b\"\n" +
      "new file mode 100644\n" +
      "index 0000000..8cb0607\n" +
      "--- /dev/null\n" +
      "+++ \"b/\\303\\272\\303\\274\\303\\276\\303\\253\\303\\251\\303\\245\\303\\253\\303\\245\\303\\251 \\303\\245g\\303\\260f\\303\\237\"\n" +
      "@@ -0,0 +1 @@\n" +
      "+String s = \"quotes shall be kept\";";

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    GitDiffCommand.DequoteOutputStream stream = new GitDiffCommand.DequoteOutputStream(buffer);
    byte[] bytes = s.getBytes();
    stream.write(bytes, 0, bytes.length);
    stream.flush();

    Assertions.assertThat(buffer.toString("UTF-8")).isEqualTo("diff --git a/file úüþëéåëåé a b/file úüþëéåëåé b\n" +
      "new file mode 100644\n" +
      "index 0000000..8cb0607\n" +
      "--- /dev/null\n" +
      "+++ b/úüþëéåëåé ågðfß\n" +
      "@@ -0,0 +1 @@\n" +
      "+String s = \"quotes shall be kept\";");
  }
}
