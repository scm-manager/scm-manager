package sonia.scm.api.v2.resources;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.MDC;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.ScmConstraintViolationException.ScmConstraintViolation;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public abstract class ScmViolationExceptionToErrorDtoMapper {

  @Mapping(target = "errorCode", ignore = true)
  @Mapping(target = "transactionId", ignore = true)
  @Mapping(target = "context", ignore = true)
  public abstract ErrorDto map(ScmConstraintViolationException exception);

  @AfterMapping
  void setTransactionId(@MappingTarget ErrorDto dto) {
    dto.setTransactionId(MDC.get("transaction_id"));
  }

  @AfterMapping
  void mapViolations(ScmConstraintViolationException exception, @MappingTarget ErrorDto dto) {
    List<ErrorDto.ConstraintViolationDto> violations =
      exception.getViolations()
        .stream()
        .map(this::createViolationDto)
        .collect(Collectors.toList());
    dto.setViolations(violations);
  }

  private ErrorDto.ConstraintViolationDto createViolationDto(ScmConstraintViolation violation) {
    ErrorDto.ConstraintViolationDto constraintViolationDto = new ErrorDto.ConstraintViolationDto();
    constraintViolationDto.setMessage(violation.getMessage());
    constraintViolationDto.setPath(violation.getPropertyPath());
    return constraintViolationDto;
  }

  @AfterMapping
  void setErrorCode(@MappingTarget ErrorDto dto) {
    dto.setErrorCode("3zR9vPNIE1");
  }

  @AfterMapping
  void setMessage(@MappingTarget ErrorDto dto) {
    dto.setMessage("input violates conditions (see violation list)");
  }
}
