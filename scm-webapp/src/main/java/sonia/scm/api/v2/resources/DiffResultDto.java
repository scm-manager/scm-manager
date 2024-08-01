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
