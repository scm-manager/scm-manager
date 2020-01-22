package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.otto.edison.hal.HalRepresentation;
import lombok.Data;

import java.util.List;

@Data
public class DiffResultDto extends HalRepresentation {

  private List<FileDto> files;

  @Data
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  public static class FileDto {

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
    private List<HunkDto> hunks;

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
