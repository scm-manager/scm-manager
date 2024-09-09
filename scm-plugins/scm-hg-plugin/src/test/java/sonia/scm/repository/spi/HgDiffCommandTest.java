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

package sonia.scm.repository.spi;

import org.junit.Test;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
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
  public void shouldCreateDiffInternal() throws IOException {
    DiffCommandRequest request = new DiffCommandRequest();
    request.setRevision("3049df33fdbbded08b707bac3eccd0f7b453c58b");

    String content = getStringFromDiffStream(new HgDiffCommand(cmdContext).getDiffResultInternal(request));
    assertThat(content)
      .contains("+++ b/c/d.txt");
  }

  @Test
  public void shouldNotCloseInternalStream() {
    HgCommandContext context = spy(cmdContext);
    DiffCommandRequest request = new DiffCommandRequest();
    request.setRevision("3049df33fdbbded08b707bac3eccd0f7b453c58b");

    new HgDiffCommand(cmdContext).getDiffResultInternal(request);

    verify(context, never()).close();
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

  @Test
  public void shouldNotIgnoreWhitespaceInDefaultDiff() throws IOException {
    String content = diff(cmdContext, "2b6f8a90b33f");
    assertThat(content).contains("""
@@ -1,2 +1,2 @@
 a
-line for blame
+line  for  blame""");
  }

  @Test
  public void shouldIgnoreWhitespaceInDiff() throws IOException {
    DiffCommandRequest request = new DiffCommandRequest();
    request.setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel.ALL);
    request.setRevision("2b6f8a90b33f");
    String content = diff(cmdContext, request);
    assertThat(content).isEmpty();
  }

  private String diff(HgCommandContext context, String revision) throws IOException {
    DiffCommandRequest request = new DiffCommandRequest();
    request.setRevision(revision);
    return diff(context, request);
  }

  private String diff(HgCommandContext context, DiffCommandRequest request) throws IOException {
    HgDiffCommand diff = new HgDiffCommand(context);
    DiffCommandBuilder.OutputStreamConsumer consumer = diff.getDiffResult(request);
    return getStringFromDiffStream(consumer);
  }

  private String getStringFromDiffStream(DiffCommandBuilder.OutputStreamConsumer consumer) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    consumer.accept(baos);
    return baos.toString(UTF_8);
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-hg-spi-diff-test.zip";
  }
}
