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

package sonia.scm.api;

import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;

import java.util.Collections;

class ErrorDtos {

  private ErrorDtos() {
  }

  static ErrorDto from(String code, Exception exception) {
    ErrorDto errorDto = new ErrorDto();
    errorDto.setMessage(exception.getMessage());
    errorDto.setContext(Collections.emptyList());
    errorDto.setErrorCode(code);
    errorDto.setTransactionId(MDC.get("transaction_id"));
    return errorDto;
  }
}
