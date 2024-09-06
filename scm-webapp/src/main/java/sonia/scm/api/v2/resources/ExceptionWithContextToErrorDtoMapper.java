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
import sonia.scm.ExceptionWithContext;

import java.util.Optional;

@Mapper
public interface ExceptionWithContextToErrorDtoMapper {

  @Mapping(target = "errorCode", source = "code")
  @Mapping(target = "transactionId", ignore = true)
  @Mapping(target = "violations", ignore = true)
  ErrorDto map(ExceptionWithContext exception);

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // is ok for mapping
  default String mapOptional(Optional<String> optionalString) {
    return optionalString.orElse(null);
  }

  @AfterMapping
  default void setTransactionId(@MappingTarget ErrorDto dto) {
    dto.setTransactionId(MDC.get("transaction_id"));
  }

  ErrorDto.AdditionalMessageDto map(ExceptionWithContext.AdditionalMessage message);
}
