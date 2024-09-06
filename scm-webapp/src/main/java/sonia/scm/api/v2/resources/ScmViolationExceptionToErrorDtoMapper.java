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

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.MDC;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.ScmConstraintViolationException.ScmConstraintViolation;

@Mapper
public interface ScmViolationExceptionToErrorDtoMapper {

  @Mapping(target = "errorCode", ignore = true)
  @Mapping(target = "transactionId", ignore = true)
  @Mapping(target = "context", ignore = true)
  @Mapping(target = "additionalMessages", ignore = true)
  ErrorDto map(ScmConstraintViolationException exception);

  @AfterMapping
  default void setTransactionId(@MappingTarget ErrorDto dto) {
    dto.setTransactionId(MDC.get("transaction_id"));
  }

  @Mapping(source = "propertyPath", target = "path")
  ErrorDto.ConstraintViolationDto map(ScmConstraintViolation violation);

  @AfterMapping
  default void setErrorCode(@MappingTarget ErrorDto dto) {
    dto.setErrorCode("3zR9vPNIE1");
  }

  @AfterMapping
  default void setMessage(@MappingTarget ErrorDto dto) {
    dto.setMessage("input violates conditions (see violation list)");
  }
}
