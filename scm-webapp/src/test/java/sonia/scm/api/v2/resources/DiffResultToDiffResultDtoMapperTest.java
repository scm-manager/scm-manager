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
    
package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Link;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffLine;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.Hunk;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static java.net.URI.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiffResultToDiffResultDtoMapperTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "space", "X");

  ResourceLinks resourceLinks = ResourceLinksMock.createMock(create("/scm/api/v2"));
  DiffResultToDiffResultDtoMapper mapper = new DiffResultToDiffResultDtoMapper(resourceLinks);

  @Test
  void shouldMapDiffResult() {
    DiffResultDto dto = mapper.mapForRevision(REPOSITORY, createResult(), "123");

    List<DiffResultDto.FileDto> files = dto.getFiles();
    assertAddedFile(files.get(0), "A.java", "abc", "java");
    assertModifiedFile(files.get(1), "B.ts", "abc", "def", "typescript");
    assertDeletedFile(files.get(2), "C.go", "ghi", "golang");

    DiffResultDto.HunkDto hunk = files.get(1).getHunks().get(0);
    assertHunk(hunk, "@@ -3,4 1,2 @@", 1, 2, 3, 4);

    List<DiffResultDto.ChangeDto> changes = hunk.getChanges();
    assertInsertedLine(changes.get(0), "a", 1);
    assertModifiedLine(changes.get(1), "b", 2);
    assertDeletedLine(changes.get(2), "c", 3);
  }

  @Test
  void shouldCreateSelfLinkForRevision() {
    DiffResultDto dto = mapper.mapForRevision(REPOSITORY, createResult(), "123");

    Optional<Link> selfLink = dto.getLinks().getLinkBy("self");
    assertThat(selfLink)
      .isPresent()
      .get()
      .extracting("href")
      .isEqualTo("/scm/api/v2/repositories/space/X/diff/123/parsed");
  }

  @Test
  void shouldCreateSelfLinkForIncoming() {
    DiffResultDto dto = mapper.mapForIncoming(REPOSITORY, createResult(), "feature/some", "master");

    Optional<Link> selfLink = dto.getLinks().getLinkBy("self");
    assertThat(selfLink)
      .isPresent()
      .get()
      .extracting("href")
      .isEqualTo("/scm/api/v2/repositories/space/X/incoming/feature%2Fsome/master/diff/parsed");
  }

  private DiffResult createResult() {
    return result(
      addedFile("A.java", "abc"),
      modifiedFile("B.ts", "def", "abc",
        hunk("@@ -3,4 1,2 @@", 1, 2, 3, 4,
          insertedLine("a", 1),
          modifiedLine("b", 2),
          deletedLine("c", 3)
        )
      ),
      deletedFile("C.go", "ghi")
    );
  }

  public void assertInsertedLine(DiffResultDto.ChangeDto change, String content, int lineNumber) {
    assertThat(change.getContent()).isEqualTo(content);
    assertThat(change.getLineNumber()).isEqualTo(lineNumber);
    assertThat(change.getType()).isEqualTo("insert");
    assertThat(change.isInsert()).isTrue();
  }

  private void assertModifiedLine(DiffResultDto.ChangeDto change, String content, int lineNumber) {
    assertThat(change.getContent()).isEqualTo(content);
    assertThat(change.getNewLineNumber()).isEqualTo(lineNumber);
    assertThat(change.getOldLineNumber()).isEqualTo(lineNumber);
    assertThat(change.getType()).isEqualTo("normal");
    assertThat(change.isNormal()).isTrue();
  }

  private void assertDeletedLine(DiffResultDto.ChangeDto change, String content, int lineNumber) {
    assertThat(change.getContent()).isEqualTo(content);
    assertThat(change.getLineNumber()).isEqualTo(lineNumber);
    assertThat(change.getType()).isEqualTo("delete");
    assertThat(change.isDelete()).isTrue();
  }

  private void assertHunk(DiffResultDto.HunkDto hunk, String content, int newStart, int newLineCount, int oldStart, int oldLineCount) {
    assertThat(hunk.getContent()).isEqualTo(content);
    assertThat(hunk.getNewStart()).isEqualTo(newStart);
    assertThat(hunk.getNewLines()).isEqualTo(newLineCount);
    assertThat(hunk.getOldStart()).isEqualTo(oldStart);
    assertThat(hunk.getOldLines()).isEqualTo(oldLineCount);
  }

  private void assertAddedFile(DiffResultDto.FileDto file, String path, String revision, String language) {
    assertThat(file.getNewPath()).isEqualTo(path);
    assertThat(file.getNewRevision()).isEqualTo(revision);
    assertThat(file.getType()).isEqualTo("add");
    assertThat(file.getLanguage()).isEqualTo(language);
  }

  private void assertModifiedFile(DiffResultDto.FileDto file, String path, String oldRevision, String newRevision, String language) {
    assertThat(file.getNewPath()).isEqualTo(path);
    assertThat(file.getNewRevision()).isEqualTo(newRevision);
    assertThat(file.getOldPath()).isEqualTo(path);
    assertThat(file.getOldRevision()).isEqualTo(oldRevision);
    assertThat(file.getType()).isEqualTo("modify");
    assertThat(file.getLanguage()).isEqualTo(language);
  }

  private void assertDeletedFile(DiffResultDto.FileDto file, String path, String revision, String language) {
    assertThat(file.getOldPath()).isEqualTo(path);
    assertThat(file.getOldRevision()).isEqualTo(revision);
    assertThat(file.getType()).isEqualTo("delete");
    assertThat(file.getLanguage()).isEqualTo(language);
  }

  private DiffResult result(DiffFile... files) {
    DiffResult result = mock(DiffResult.class);
    when(result.iterator()).thenReturn(Arrays.asList(files).iterator());
    return result;
  }

  private DiffFile addedFile(String path, String revision, Hunk... hunks) {
    DiffFile file = mock(DiffFile.class);
    when(file.getNewPath()).thenReturn(path);
    when(file.getNewRevision()).thenReturn(revision);
    when(file.iterator()).thenReturn(Arrays.asList(hunks).iterator());
    return file;
  }

  private DiffFile deletedFile(String path, String revision, Hunk... hunks) {
    DiffFile file = mock(DiffFile.class);
    when(file.getOldPath()).thenReturn(path);
    when(file.getOldRevision()).thenReturn(revision);
    when(file.iterator()).thenReturn(Arrays.asList(hunks).iterator());
    return file;
  }

  private DiffFile modifiedFile(String path, String newRevision, String oldRevision, Hunk... hunks) {
    DiffFile file = mock(DiffFile.class);
    when(file.getNewPath()).thenReturn(path);
    when(file.getNewRevision()).thenReturn(newRevision);
    when(file.getOldPath()).thenReturn(path);
    when(file.getOldRevision()).thenReturn(oldRevision);
    when(file.iterator()).thenReturn(Arrays.asList(hunks).iterator());
    return file;
  }

  private Hunk hunk(String rawHeader, int newStart, int newLineCount, int oldStart, int oldLineCount, DiffLine... lines) {
    Hunk hunk = mock(Hunk.class);
    when(hunk.getRawHeader()).thenReturn(rawHeader);
    when(hunk.getNewStart()).thenReturn(newStart);
    when(hunk.getNewLineCount()).thenReturn(newLineCount);
    when(hunk.getOldStart()).thenReturn(oldStart);
    when(hunk.getOldLineCount()).thenReturn(oldLineCount);
    when(hunk.iterator()).thenReturn(Arrays.asList(lines).iterator());
    return hunk;
  }

  private DiffLine insertedLine(String content, int lineNumber) {
    DiffLine line = mock(DiffLine.class);
    when(line.getContent()).thenReturn(content);
    when(line.getNewLineNumber()).thenReturn(OptionalInt.of(lineNumber));
    when(line.getOldLineNumber()).thenReturn(OptionalInt.empty());
    return line;
  }

  private DiffLine modifiedLine(String content, int lineNumber) {
    DiffLine line = mock(DiffLine.class);
    when(line.getContent()).thenReturn(content);
    when(line.getNewLineNumber()).thenReturn(OptionalInt.of(lineNumber));
    when(line.getOldLineNumber()).thenReturn(OptionalInt.of(lineNumber));
    return line;
  }

  private DiffLine deletedLine(String content, int lineNumber) {
    DiffLine line = mock(DiffLine.class);
    when(line.getContent()).thenReturn(content);
    when(line.getNewLineNumber()).thenReturn(OptionalInt.empty());
    when(line.getOldLineNumber()).thenReturn(OptionalInt.of(lineNumber));
    return line;
  }
}
