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
