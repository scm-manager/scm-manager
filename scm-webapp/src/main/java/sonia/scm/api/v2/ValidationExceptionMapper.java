package sonia.scm.api.v2;

import lombok.Getter;
import org.jboss.resteasy.api.validation.ResteasyViolationException;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {

  @Override
  public Response toResponse(ResteasyViolationException exception) {

    List<ConstraintViolationBean> violations =
      exception.getConstraintViolations()
        .stream()
        .map(ConstraintViolationBean::new)
        .collect(Collectors.toList());

    return Response
      .status(Response.Status.BAD_REQUEST)
      .type(MediaType.APPLICATION_JSON_TYPE)
      .entity(new ValidationError(violations))
      .build();
  }

  @Getter
  public static class ValidationError {
    @XmlElement(name = "violation")
    @XmlElementWrapper(name = "violations")
    private List<ConstraintViolationBean> violations;

    public ValidationError(List<ConstraintViolationBean> violations) {
      this.violations = violations;
    }
  }

  @XmlRootElement(name = "violation")
  @Getter
  public static class ConstraintViolationBean {
    private String path;
    private String message;

    public ConstraintViolationBean(ConstraintViolation<?> violation) {
      message = violation.getMessage();
      path = violation.getPropertyPath().toString();
    }
  }
}
