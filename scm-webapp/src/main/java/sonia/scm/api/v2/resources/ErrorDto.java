package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.ContextEntry;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter @Setter
public class ErrorDto {
  private String transactionId;
  private String errorCode;
  private List<ContextEntry> context;
  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @XmlElementWrapper(name = "violations")
  private List<ConstraintViolationDto> violations;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String url;

  @XmlRootElement(name = "violation")
  @Getter @Setter
  public static class ConstraintViolationDto {
    private String path;
    private String message;
  }
}
