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

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

  private static final Logger LOG = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

  private static final String ERROR_CODE = "FVS9JY1T21";

  @Override
  public Response toResponse(WebApplicationException exception) {
    LOG.trace("caught web application exception", exception);


    ErrorDto errorDto = ErrorDtos.from(ERROR_CODE, exception);
    Response originalResponse = exception.getResponse();

    return Response.fromResponse(originalResponse)
      .entity(errorDto)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
