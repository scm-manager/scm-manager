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

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import java.util.Collections;

@Provider
public class InvalidFormatExceptionMapper implements ExceptionMapper<InvalidFormatException> {

  private static final Logger LOG = LoggerFactory.getLogger(InvalidFormatExceptionMapper.class);

  private static final String ERROR_CODE = "2qRyyaVcJ1";

  @Override
  public Response toResponse(InvalidFormatException exception) {
    LOG.trace("got invalid format in json: {}", exception.getMessage());
    ErrorDto errorDto = new ErrorDto();
    errorDto.setMessage("invalid format in json content: " + exception.getMessage());
    errorDto.setContext(Collections.emptyList());
    errorDto.setErrorCode(ERROR_CODE);
    errorDto.setTransactionId(MDC.get("transaction_id"));
    return Response.status(Response.Status.BAD_REQUEST)
      .entity(errorDto)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
