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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sonia.scm.repository.api.DiffResult;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class DiffResultDto extends HalRepresentation {

  public DiffResultDto(Links links) {
    super(links);
  }

  private List<FileDto> files;
  private boolean partial;
  private DiffStatisticsDto statistics;

  @Data
  @EqualsAndHashCode(callSuper = false)
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  public static class FileDto extends HalRepresentation {

    public FileDto(Links links) {
      super(links);
    }

    private String oldPath;
    private String newPath;
    private boolean oldEndingNewLine;
    private boolean newEndingNewLine;
    private String oldRevision;
    private String newRevision;
    private String newMode;
    private String oldMode;
    private String type;
    private String language;
    private Map<String, String> syntaxModes;
    private List<HunkDto> hunks;

  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  @AllArgsConstructor
  public static class DiffStatisticsDto {
    private int added;
    private int deleted;
    private int modified;
  }

  @Data
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  public static class HunkDto {

    private String content;
    private int oldStart;
    private int newStart;
    private int oldLines;
    private int newLines;
    private List<ChangeDto> changes;

  }

  @Data
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  public static class ChangeDto {

    private String content;
    private String type;
    @JsonProperty("isNormal")
    private boolean isNormal;
    @JsonProperty("isInsert")
    private boolean isInsert;
    @JsonProperty("isDelete")
    private boolean isDelete;
    private int lineNumber;
    private int oldLineNumber;
    private int newLineNumber;

  }

}
