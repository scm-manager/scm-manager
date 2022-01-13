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
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class HgDiffCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void shouldCreateDiff() throws IOException {
    String content = diff(cmdContext, "3049df33fdbbded08b707bac3eccd0f7b453c58b");
    assertThat(content).contains("+e");
  }

  @Test
  public void shouldCreateGitDiff() throws IOException {
    DiffCommandRequest request = new DiffCommandRequest();
    request.setRevision("3049df33fdbbded08b707bac3eccd0f7b453c58b");
    request.setFormat(DiffFormat.GIT);

    String content = diff(cmdContext, request);
    assertThat(content).contains("git");
  }

  @Test
  public void shouldCreateDiffComparedToAncestor() throws IOException {
    DiffCommandRequest request = new DiffCommandRequest();
    request.setRevision("3049df33fdbbded08b707bac3eccd0f7b453c58b");
    request.setAncestorChangeset("a9bacaf1b7fa0cebfca71fed4e59ed69a6319427");

    String content = diff(cmdContext, request);
    assertThat(content)
      .contains("+++ b/c/d.txt")
      .contains("+++ b/c/e.txt");
  }

  @Test
  public void shouldNotCreateDiffWithAncestorIfNoChangesExists() throws IOException {
    DiffCommandRequest request = new DiffCommandRequest();
    request.setRevision("a9bacaf1b7fa0cebfca71fed4e59ed69a6319427");
    request.setAncestorChangeset("3049df33fdbbded08b707bac3eccd0f7b453c58b");

    String content = diff(cmdContext, request);
    assertThat(content).isEmpty();
  }

  @Test
  public void shouldCloseContent() throws IOException {
    HgCommandContext context = spy(cmdContext);
    String content = diff(context, "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427");
    assertThat(content).contains("+b");
    verify(context).close();
  }

  private String diff(HgCommandContext context, String revision) throws IOException {
    DiffCommandRequest request = new DiffCommandRequest();
    request.setRevision(revision);
    return diff(context, request);
  }

  private String diff(HgCommandContext context, DiffCommandRequest request) throws IOException {
    HgDiffCommand diff = new HgDiffCommand(context);
    DiffCommandBuilder.OutputStreamConsumer consumer = diff.getDiffResult(request);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    consumer.accept(baos);
    return baos.toString("UTF-8");
  }

}
