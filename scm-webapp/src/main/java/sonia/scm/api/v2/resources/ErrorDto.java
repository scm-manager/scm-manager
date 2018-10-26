package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.slf4j.MDC;
import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;

import java.util.List;

@Getter
public class ErrorDto {
  private final String transactionId;
  private final String errorCode;
  private final List<ContextEntry> context;
  private final String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final String url;

  protected ErrorDto(String transactionId, String errorCode, List<ContextEntry> context, String message) {
    this(transactionId, errorCode, context, message, null);
  }
  private ErrorDto(String transactionId, String errorCode, List<ContextEntry> context, String message, String url) {
    this.transactionId = transactionId;
    this.errorCode = errorCode;
    this.context = context;
    this.message = message;
    this.url = url;
  }

  public static ErrorDto from(ExceptionWithContext exception) {
    return new ErrorDto(MDC.get("transaction_id"), exception.getCode(), exception.getContext(), exception.getMessage());
  }
}
