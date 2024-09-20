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
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.Hunk;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.OPTIONAL;
import static org.junit.Assert.assertEquals;

public class GitDiffResultCommandTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldReturnOldAndNewRevision() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    assertThat(diffResult.getNewRevision()).isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    assertThat(diffResult.getOldRevision()).isEqualTo("592d797cd36432e591416e8b2b98154f4f163411");
  }

  @Test
  public void shouldReturnFilePaths() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    Iterator<DiffFile> iterator = diffResult.iterator();
    DiffFile a = iterator.next();
    assertThat(a.getNewPath()).isEqualTo("a.txt");
    assertThat(a.getOldPath()).isEqualTo("a.txt");

    DiffFile b = iterator.next();
    assertThat(b.getOldPath()).isEqualTo("b.txt");
    assertThat(b.getNewPath()).isEqualTo("/dev/null");

    assertThat(diffResult.isPartial()).isFalse();
  }

  @Test
  public void shouldReturnFileRevisions() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile a = iterator.next();
    assertThat(a.getOldRevision()).isEqualTo("592d797cd36432e591416e8b2b98154f4f163411");
    assertThat(a.getNewRevision()).isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    DiffFile b = iterator.next();
    assertThat(b.getOldRevision()).isEqualTo("592d797cd36432e591416e8b2b98154f4f163411");
    assertThat(b.getNewRevision()).isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    assertThat(iterator.hasNext()).isFalse();
  }

  @Test
  public void shouldReturnFileHunks() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile a = iterator.next();
    Iterator<Hunk> hunks = a.iterator();

    Hunk hunk = hunks.next();
    assertThat(hunk.getOldStart()).isEqualTo(1);
    assertThat(hunk.getOldLineCount()).isEqualTo(1);

    assertThat(hunk.getNewStart()).isEqualTo(1);
    assertThat(hunk.getNewLineCount()).isEqualTo(1);
  }

  @Test
  public void shouldReturnFileHunksWithFullFileRange() throws IOException {
    DiffResult diffResult = createDiffResult("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile a = iterator.next();
    Iterator<Hunk> hunks = a.iterator();

    Hunk hunk = hunks.next();
    assertThat(hunk.getOldStart()).isEqualTo(1);
    assertThat(hunk.getOldLineCount()).isEqualTo(1);

    assertThat(hunk.getNewStart()).isEqualTo(1);
    assertThat(hunk.getNewLineCount()).isEqualTo(2);
  }

  @Test
  public void shouldReturnRenames() throws IOException {
    DiffResult diffResult = createDiffResult("rename");

    Iterator<DiffFile> fileIterator = diffResult.iterator();
    DiffFile renameA = fileIterator.next();
    assertThat(renameA.getOldPath()).isEqualTo("a.txt");
    assertThat(renameA.getNewPath()).isEqualTo("a-copy.txt");
    assertThat(renameA.iterator().hasNext()).isFalse();

    DiffFile renameB = fileIterator.next();
    assertThat(renameB.getOldPath()).isEqualTo("b.txt");
    assertThat(renameB.getNewPath()).isEqualTo("b-copy.txt");
    assertThat(renameB.iterator().hasNext()).isFalse();
  }

  @Test
  public void shouldLimitResult() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4", null, 1);
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile a = iterator.next();
    assertThat(a.getNewPath()).isEqualTo("a.txt");
    assertThat(a.getOldPath()).isEqualTo("a.txt");

    assertThat(iterator.hasNext()).isFalse();

    assertThat(diffResult.isPartial()).isTrue();
    assertThat(diffResult.getLimit()).get().isEqualTo(1);
    assertThat(diffResult.getOffset()).isZero();
  }

  @Test
  public void shouldSetOffsetForResult() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4", 1, null);
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile b = iterator.next();
    assertThat(b.getOldPath()).isEqualTo("b.txt");
    assertThat(b.getNewPath()).isEqualTo("/dev/null");

    assertThat(iterator.hasNext()).isFalse();

    assertThat(diffResult.isPartial()).isFalse();
  }

  @Test
  public void shouldNotBePartialWhenResultCountMatchesLimit() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4", 0, 2);

    assertThat(diffResult.isPartial()).isFalse();
    assertThat(diffResult.getLimit()).get().isEqualTo(2);
    assertThat(diffResult.getOffset()).isZero();
  }

  @Test
  public void shouldIgnoreWhiteSpace() throws IOException {
    GitDiffResultCommand gitDiffResultCommand = new GitDiffResultCommand(createContext());
    DiffResultCommandRequest diffCommandRequest = new DiffResultCommandRequest();
    diffCommandRequest.setRevision("whitespace");
    diffCommandRequest.setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel.ALL);

    DiffResult diffResult = gitDiffResultCommand.getDiffResult(diffCommandRequest);
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile a = iterator.next();
    Iterator<Hunk> hunks = a.iterator();

    assertThat(hunks).isExhausted();
  }

  @Test
  public void shouldComputeStatistics() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    assertThat(diffResult.getStatistics()).get().extracting("deleted").isEqualTo(1);
    assertThat(diffResult.getStatistics()).get().extracting("modified").isEqualTo(1);
    assertThat(diffResult.getStatistics()).get().extracting("added").isEqualTo(0);
    assertThat(diffResult.getStatistics()).get().extracting("renamed").isEqualTo(0);
    assertThat(diffResult.getStatistics()).get().extracting("copied").isEqualTo(0);
  }

  @Test
  public void shouldCreateFileTree() throws IOException {
    DiffResult.DiffTreeNode root = DiffResult.DiffTreeNode.createRootNode();
    root.addChild("a.txt", DiffFile.ChangeType.MODIFY);
    root.addChild("b.txt", DiffFile.ChangeType.DELETE);
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    assertEquals(Optional.of(root),diffResult.getDiffTree());
  }
  
  @Test
  public void shouldNotIgnoreWhiteSpace() throws IOException {
    GitDiffResultCommand gitDiffResultCommand = new GitDiffResultCommand(createContext());
    DiffResultCommandRequest diffCommandRequest = new DiffResultCommandRequest();
    diffCommandRequest.setRevision("whitespace");
    diffCommandRequest.setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel.NONE);

    DiffResult diffResult = gitDiffResultCommand.getDiffResult(diffCommandRequest);
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile a = iterator.next();
    Iterator<Hunk> hunks = a.iterator();

    Hunk hunk = hunks.next();
    assertThat(hunk.getOldStart()).isEqualTo(1);
    assertThat(hunk.getOldLineCount()).isEqualTo(2);
    assertThat(hunk.iterator())
      .toIterable()
      .extracting("content")
      .containsExactly(
        "a",
        "line for blame",
        "line                          for blame"
      );
  }

  private DiffResult createDiffResult(String s) throws IOException {
    return createDiffResult(s, null, null);
  }

  private DiffResult createDiffResult(String s, Integer offset, Integer limit) throws IOException {
    GitDiffResultCommand gitDiffResultCommand = new GitDiffResultCommand(createContext());
    DiffResultCommandRequest diffCommandRequest = new DiffResultCommandRequest();
    diffCommandRequest.setRevision(s);
    diffCommandRequest.setOffset(offset);
    diffCommandRequest.setLimit(limit);

    return gitDiffResultCommand.getDiffResult(diffCommandRequest);
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-whitespace-test.zip";
  }
}
