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
