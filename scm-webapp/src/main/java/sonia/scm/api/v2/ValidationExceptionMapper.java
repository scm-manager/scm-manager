package sonia.scm.api.v2;

import lombok.Getter;
import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {

  @Override
  public Response toResponse(ResteasyViolationException exception) {

    List<ConstraintViolationDto> violations =
      exception.getConstraintViolations()
        .stream()
        .map(ConstraintViolationDto::new)
        .collect(Collectors.toList());

    return Response
      .status(Response.Status.BAD_REQUEST)
      .type(MediaType.APPLICATION_JSON_TYPE)
      .entity(new ValidationErrorDto(violations))
      .build();
  }

  @Getter
  public static class ValidationErrorDto extends ErrorDto {
    @XmlElement(name = "violation")
    @XmlElementWrapper(name = "violations")
    private List<ConstraintViolationDto> violations;

    public ValidationErrorDto(List<ConstraintViolationDto> violations) {
      super(MDC.get("transaction_id"), "1wR7ZBe7H1", emptyList(), "input violates conditions (see violation list)");
      this.violations = violations;
    }
  }

  @XmlRootElement(name = "violation")
  @Getter
  public static class ConstraintViolationDto {
    private String path;
    private String message;

    public ConstraintViolationDto(ConstraintViolation<?> violation) {
      message = violation.getMessage();
      path = violation.getPropertyPath().toString();
    }
  }
}
