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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.MDC;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public abstract class ResteasyViolationExceptionToErrorDtoMapper {

  @Mapping(target = "errorCode", ignore = true)
  @Mapping(target = "transactionId", ignore = true)
  @Mapping(target = "context", ignore = true)
  @Mapping(target = "url", ignore = true)
  @Mapping(target = "violations", ignore = true)
  @Mapping(target = "additionalMessages", ignore = true)
  public abstract ErrorDto map(ConstraintViolationException exception);

  @AfterMapping
  void setTransactionId(@MappingTarget ErrorDto dto) {
    dto.setTransactionId(MDC.get("transaction_id"));
  }

  @AfterMapping
  void mapViolations(ConstraintViolationException exception, @MappingTarget ErrorDto dto) {
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
