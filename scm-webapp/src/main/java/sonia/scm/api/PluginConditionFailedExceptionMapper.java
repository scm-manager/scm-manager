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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.plugin.PluginCondition;
import sonia.scm.plugin.PluginConditionFailedException;
import sonia.scm.web.VndMediaType;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Slf4j
@Provider
public class PluginConditionFailedExceptionMapper implements ExceptionMapper<PluginConditionFailedException> {

  private static final String ERROR_CODE_VERSION = "5HU4ouwQr1";
  private static final String ERROR_CODE_OS = "5gU4pegil1";
  private static final String ERROR_CODE_ARCH = "DHU4pfOtA1";

  @Override
  public Response toResponse(PluginConditionFailedException exception) {
    log.warn("got plugin condition mismatch", exception);
    ErrorDto errorDto = new ErrorDto();
    errorDto.setMessage(exception.getMessage());
    errorDto.setContext(entity("Plugin", exception.getPluginName()).build());

    PluginCondition.CheckResult pluginConditionCheckResult = exception.getCondition().getConditionCheckResult();
    switch (pluginConditionCheckResult) {
      case VERSION_MISMATCH ->  errorDto.setErrorCode(ERROR_CODE_VERSION);
      case OS_MISMATCH -> errorDto.setErrorCode(ERROR_CODE_OS);
      case ARCHITECTURE_MISMATCH -> errorDto.setErrorCode(ERROR_CODE_ARCH);
    }

    errorDto.setTransactionId(MDC.get("transaction_id"));
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity(errorDto)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
