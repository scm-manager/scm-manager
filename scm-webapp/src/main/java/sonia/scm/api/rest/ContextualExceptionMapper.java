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
    
package sonia.scm.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ExceptionWithContext;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ContextualExceptionMapper<E extends ExceptionWithContext> implements ExceptionMapper<E> {

  private static final Logger logger = LoggerFactory.getLogger(ContextualExceptionMapper.class);

  private final ExceptionWithContextToErrorDtoMapper mapper;

  private final Response.Status status;
  private final Class<E> type;

  public ContextualExceptionMapper(Class<E> type, Response.Status status, ExceptionWithContextToErrorDtoMapper mapper) {
    this.mapper = mapper;
    this.type = type;
    this.status = status;
  }

  @Override
  public Response toResponse(E exception) {
    if (logger.isTraceEnabled()) {
      logger.trace("map {} to status code {}", type.getSimpleName(), status.getStatusCode(), exception);
    } else {
      logger.debug("map {} to status code {} with message '{}'", type.getSimpleName(), status.getStatusCode(), exception.getMessage());
    }
    return Response.status(status)
      .entity(mapper.map(exception))
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
