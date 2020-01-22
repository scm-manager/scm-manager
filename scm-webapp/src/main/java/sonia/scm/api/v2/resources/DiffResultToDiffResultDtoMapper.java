package sonia.scm.api.v2.resources;

import com.github.sdorra.spotter.ContentTypes;
import com.github.sdorra.spotter.Language;
import com.google.common.base.Strings;
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffLine;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.Hunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * TODO conflicts, copy and rename
 */
final class DiffResultToDiffResultDtoMapper {

  static final DiffResultToDiffResultDtoMapper INSTANCE = new DiffResultToDiffResultDtoMapper();

  private DiffResultToDiffResultDtoMapper() {
  }

  public DiffResultDto map(DiffResult result) {
    List<DiffResultDto.FileDto> files = new ArrayList<>();
    for (DiffFile file : result) {
      files.add(mapFile(file));
    }
    DiffResultDto dto = new DiffResultDto();
    dto.setFiles(files);
    return dto;
  }

  private DiffResultDto.FileDto mapFile(DiffFile file) {
    DiffResultDto.FileDto dto = new DiffResultDto.FileDto();
    // ???
    dto.setOldEndingNewLine(true);
    dto.setNewEndingNewLine(true);

    String newPath = file.getNewPath();
    String oldPath = file.getOldPath();

    String path;
    if (isFilePath(newPath) && isFileNull(oldPath)) {
      path = newPath;
      dto.setType("add");
    } else if (isFileNull(newPath) && isFilePath(oldPath)) {
      path = oldPath;
      dto.setType("delete");
    } else if (isFilePath(newPath) && isFilePath(oldPath)) {
      path = newPath;
      dto.setType("modify");
    } else {
      // TODO copy and rename?
      throw new IllegalStateException("no file without path");
    }

    dto.setNewPath(newPath);
    dto.setNewRevision(file.getNewRevision());

    dto.setOldPath(oldPath);
    dto.setOldRevision(file.getOldRevision());


    Optional<Language> language = ContentTypes.detect(path).getLanguage();
    language.ifPresent(value -> dto.setLanguage(value.getName()));

    List<DiffResultDto.HunkDto> hunks = new ArrayList<>();
    for (Hunk hunk : file) {
      hunks.add(mapHunk(hunk));
    }
    dto.setHunks(hunks);

    return dto;
  }

  private boolean isFilePath(String path) {
    return !isFileNull(path);
  }

  private boolean isFileNull(String path) {
    return Strings.isNullOrEmpty(path) || "/dev/null".equals(path);
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
