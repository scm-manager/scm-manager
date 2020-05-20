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

import com.github.sdorra.spotter.ContentTypes;
import com.github.sdorra.spotter.Language;
import com.google.inject.Inject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffLine;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.Hunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static de.otto.edison.hal.Links.linkingTo;

/**
 * TODO conflicts
 */
class DiffResultToDiffResultDtoMapper {

  private final ResourceLinks resourceLinks;

  @Inject
  DiffResultToDiffResultDtoMapper(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public DiffResultDto mapForIncoming(Repository repository, DiffResult result, String source, String target) {
    DiffResultDto dto = new DiffResultDto(linkingTo().self(resourceLinks.incoming().diffParsed(repository.getNamespace(), repository.getName(), source, target)).build());
    setFiles(result, dto);
    return dto;
  }

  public DiffResultDto mapForRevision(Repository repository, DiffResult result, String revision) {
    DiffResultDto dto = new DiffResultDto(linkingTo().self(resourceLinks.diff().parsed(repository.getNamespace(), repository.getName(), revision)).build());
    setFiles(result, dto);
    return dto;
  }

  private void setFiles(DiffResult result, DiffResultDto dto) {
    List<DiffResultDto.FileDto> files = new ArrayList<>();
    for (DiffFile file : result) {
      files.add(mapFile(file));
    }
    dto.setFiles(files);
  }

  private DiffResultDto.FileDto mapFile(DiffFile file) {
    DiffResultDto.FileDto dto = new DiffResultDto.FileDto();
    // ???
    dto.setOldEndingNewLine(true);
    dto.setNewEndingNewLine(true);

    String newPath = file.getNewPath();
    String oldPath = file.getOldPath();

    String path;
    switch (file.getChangeType()) {
      case ADD:
        path = newPath;
        dto.setType("add");
        break;
      case DELETE:
        path = oldPath;
        dto.setType("delete");
        break;
      case RENAME:
        path = newPath;
        dto.setType("rename");
        break;
      case MODIFY:
        path = newPath;
        dto.setType("modify");
        break;
      case COPY:
        path = newPath;
        dto.setType("copy");
        break;
      default:
        throw new IllegalArgumentException("unknown change type: " + file.getChangeType());
    }

    dto.setNewPath(newPath);
    dto.setNewRevision(file.getNewRevision());

    dto.setOldPath(oldPath);
    dto.setOldRevision(file.getOldRevision());


    Optional<Language> language = ContentTypes.detect(path).getLanguage();
    language.ifPresent(value -> dto.setLanguage(ProgrammingLanguages.getValue(value)));

    List<DiffResultDto.HunkDto> hunks = new ArrayList<>();
    for (Hunk hunk : file) {
      hunks.add(mapHunk(hunk));
    }
    dto.setHunks(hunks);

    return dto;
  }

  private DiffResultDto.HunkDto mapHunk(Hunk hunk) {
    DiffResultDto.HunkDto dto = new DiffResultDto.HunkDto();
    dto.setContent(hunk.getRawHeader());

    dto.setNewStart(hunk.getNewStart());
    dto.setNewLines(hunk.getNewLineCount());

    dto.setOldStart(hunk.getOldStart());
    dto.setOldLines(hunk.getOldLineCount());

    List<DiffResultDto.ChangeDto> changes = new ArrayList<>();
    for (DiffLine line : hunk) {
      changes.add(mapLine(line));
    }

    dto.setChanges(changes);
    return dto;
  }

  private DiffResultDto.ChangeDto mapLine(DiffLine line) {
    DiffResultDto.ChangeDto dto = new DiffResultDto.ChangeDto();
    dto.setContent(line.getContent());

    OptionalInt newLineNumber = line.getNewLineNumber();
    OptionalInt oldLineNumber = line.getOldLineNumber();
    if (newLineNumber.isPresent() && !oldLineNumber.isPresent()) {
      dto.setType("insert");
      dto.setInsert(true);
      dto.setLineNumber(newLineNumber.getAsInt());
    } else if (!newLineNumber.isPresent() && oldLineNumber.isPresent()) {
      dto.setType("delete");
      dto.setDelete(true);
      dto.setLineNumber(oldLineNumber.getAsInt());
    } else if (newLineNumber.isPresent() && oldLineNumber.isPresent()) {
      dto.setType("normal");
      dto.setNormal(true);
      dto.setNewLineNumber(newLineNumber.getAsInt());
      dto.setOldLineNumber(oldLineNumber.getAsInt());
    } else {
      throw new IllegalStateException("line without line number");
    }

    return dto;
  }

}
