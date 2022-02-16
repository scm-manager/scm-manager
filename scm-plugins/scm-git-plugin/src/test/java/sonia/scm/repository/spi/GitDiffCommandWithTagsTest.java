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

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GitDiffCommandWithTagsTest extends AbstractGitCommandTestBase {

  @Test
  public void diffBetweenTwoTagsShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("1.0.0");
    diffCommandRequest.setAncestorChangeset("test-tag");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals("diff --git a/a.txt b/a.txt\n" +
      "index 7898192..2f8bc28 100644\n" +
      "--- a/a.txt\n" +
      "+++ b/a.txt\n" +
      "@@ -1 +1,2 @@\n" +
      " a\n" +
      "+line for blame\n", output.toString());
  }

  @Test
  public void diffBetweenTagAndBranchShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("master");
    diffCommandRequest.setAncestorChangeset("test-tag");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals("diff --git a/a.txt b/a.txt\n" +
      "index 7898192..2f8bc28 100644\n" +
      "--- a/a.txt\n" +
      "+++ b/a.txt\n" +
      "@@ -1 +1,2 @@\n" +
      " a\n" +
      "+line for blame\n", output.toString());
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-test-tags.zip";
  }
}
