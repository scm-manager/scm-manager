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

package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Links;
import sonia.scm.io.ContentType;
import sonia.scm.io.ContentTypeResolver;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffLine;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.Hunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Link.linkBuilder;
import static de.otto.edison.hal.Links.linkingTo;

class DiffResultToDiffResultDtoMapper {

  private final ResourceLinks resourceLinks;
  private final ContentTypeResolver contentTypeResolver;

  @Inject
  DiffResultToDiffResultDtoMapper(ResourceLinks resourceLinks, ContentTypeResolver contentTypeResolver) {
    this.resourceLinks = resourceLinks;
    this.contentTypeResolver = contentTypeResolver;
  }

  public DiffResultDto mapForIncoming(Repository repository, DiffResult result, String source, String target) {
    String baseLink = resourceLinks.incoming().diffParsed(repository.getNamespace(), repository.getName(), source, target) + "?ignoreWhitespace=" + result.getIgnoreWhitespace().name();
    Links.Builder links = linkingTo().self(createSelfLink(result, baseLink));
    appendNextChunkLinkIfNeeded(links, result, baseLink);
    DiffResultDto dto = new DiffResultDto(links.build());
    setFiles(result, dto, repository, source);
    return dto;
  }

  public DiffResultDto mapForRevision(Repository repository, DiffResult result, String revision) {
    String baseLink = resourceLinks.diff().parsed(repository.getNamespace(), repository.getName(), revision) + "?ignoreWhitespace=" + result.getIgnoreWhitespace().name();
    Links.Builder links = linkingTo().self(createSelfLink(result, baseLink));
    appendNextChunkLinkIfNeeded(links, result, baseLink);
    DiffResultDto dto = new DiffResultDto(links.build());
    setFiles(result, dto, repository, revision);
    return dto;
  }

  private String createSelfLink(DiffResult result, String baseLink) {
    if (result.getOffset() > 0 || result.getLimit().isPresent()) {
      return createLinkWithLimitAndOffset(baseLink, result.getOffset(), result.getLimit().orElse(null));
    } else {
      return baseLink;
    }
  }

  private void appendNextChunkLinkIfNeeded(Links.Builder links, DiffResult result, String baseLink) {
    if (result.isPartial()) {
      Optional<Integer> limit = result.getLimit();
      if (limit.isPresent()) {
        links.single(link("next", createLinkWithLimitAndOffset(baseLink, result.getOffset() + limit.get(), limit.get())));
      } else {
        throw new IllegalStateException("a result cannot be partial without a limit");
      }
    }
  }

  private String createLinkWithLimitAndOffset(String baseLink, int offset, Integer limit) {
    if (limit == null) {
      return String.format("%s&offset=%s", baseLink, offset);
    } else {
      return String.format("%s&offset=%s&limit=%s", baseLink, offset, limit);
    }
  }

  private void setFiles(DiffResult result, DiffResultDto dto, Repository repository, String revision) {
    List<DiffResultDto.FileDto> files = new ArrayList<>();
    for (DiffFile file : result) {
      files.add(mapFile(file, result, repository, revision));
    }
    dto.setFiles(files);
    Optional<DiffResult.DiffStatistics> statistics = result.getStatistics();
    if (statistics.isPresent()) {
      DiffResult.DiffStatistics diffStatistics = statistics.get();
      DiffResultDto.DiffStatisticsDto diffStatisticsDto = new DiffResultDto.DiffStatisticsDto(
        diffStatistics.getAdded(),
        diffStatistics.getDeleted(),
        diffStatistics.getModified()
      );
      dto.setStatistics(diffStatisticsDto);
    }
    dto.setPartial(result.isPartial());
  }

  private DiffResultDto.FileDto mapFile(DiffFile file, DiffResult result, Repository repository, String revision) {
    Links.Builder links = linkingTo();
    if (file.iterator().hasNext()) {
      links.single(
        linkBuilder(
          "lines",
          resourceLinks.source().content(repository.getNamespace(), repository.getName(), revision, file.getNewPath()) + "?ignoreWhitespace=" + result.getIgnoreWhitespace().name() + "&start={start}&end={end}").build()
      );
    }
    if (!file.getChangeType().equals(DiffFile.ChangeType.ADD)) {
      links.single(linkBuilder("oldFile", resourceLinks.source().content(repository.getNamespace(), repository.getName(), file.getOldRevision(), file.getOldPath())).build());
    }
    if (!file.getChangeType().equals(DiffFile.ChangeType.DELETE)) {
      links.single(linkBuilder("newFile", resourceLinks.source().content(repository.getNamespace(), repository.getName(), file.getNewRevision(), file.getNewPath())).build());
    }
    DiffResultDto.FileDto dto = new DiffResultDto.FileDto(links.build());
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

    ContentType contentType = contentTypeResolver.resolve(path);
    Optional<String> language = contentType.getLanguage();
    language.ifPresent(dto::setLanguage);
    dto.setSyntaxModes(contentType.getSyntaxModes());

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
