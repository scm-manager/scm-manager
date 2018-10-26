package sonia.scm.api.v2.resources;

import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.MDC;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public abstract class ViolationExceptionToErrorDtoMapper {

  @Mapping(target = "errorCode", ignore = true)
  @Mapping(target = "transactionId", ignore = true)
  @Mapping(target = "context", ignore = true)
  @Mapping(target = "url", ignore = true)
  public abstract ErrorDto map(ResteasyViolationException exception);

  @AfterMapping
  void setTransactionId(@MappingTarget ErrorDto dto) {
    dto.setTransactionId(MDC.get("transaction_id"));
  }

  @AfterMapping
  void mapViolations(ResteasyViolationException exception, @MappingTarget ErrorDto dto) {
    List<ErrorDto.ConstraintViolationDto> violations =
      exception.getConstraintViolations()
        .stream()
        .map(this::createViolationDto)
        .collect(Collectors.toList());
    dto.setViolations(violations);
  }

  private ErrorDto.ConstraintViolationDto createViolationDto(ConstraintViolation<?> violation) {
    ErrorDto.ConstraintViolationDto constraintViolationDto = new ErrorDto.ConstraintViolationDto();
    constraintViolationDto.setMessage(violation.getMessage());
    constraintViolationDto.setPath(violation.getPropertyPath().toString());
    return constraintViolationDto;
  }

  @AfterMapping
  void setErrorCode(@MappingTarget ErrorDto dto) {
    dto.setErrorCode("1wR7ZBe7H1");
  }

  @AfterMapping
  void setMessage(@MappingTarget ErrorDto dto) {
    dto.setMessage("input violates conditions (see violation list)");
  }
}
