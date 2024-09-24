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

import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

@Provider
public class NotSupportedExceptionMapper implements ExceptionMapper<NotSupportedException> {

  private static final Logger LOG = LoggerFactory.getLogger(NotSupportedExceptionMapper.class);

  @Override
  public Response toResponse(NotSupportedException exception) {
    LOG.debug("illegal media type", exception);
    ErrorDto error = new ErrorDto();
    error.setTransactionId(MDC.get("transaction_id"));
    error.setMessage("illegal media type");
    error.setErrorCode("8pRBYDURx1");
    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
      .entity(error)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
