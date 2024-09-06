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

package sonia.scm.api.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ExceptionWithContext;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;
import sonia.scm.web.VndMediaType;

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
