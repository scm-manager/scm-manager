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


import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusExceptionMapper<E extends Throwable>
  implements ExceptionMapper<E>
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(StatusExceptionMapper.class);

  private final Response.Status status;
  private final Class<E> type;

  /**
   * Map an Exception to a HTTP Response
   *
   * @param type the exception class
   * @param status the http status to be mapped
   */
  public StatusExceptionMapper(Class<E> type, Response.Status status)
  {
    this.type = type;
    this.status = status;
  }

  /**
   * provide a http responses from an exception
   *
   * @param exception the thrown exception
   *
   * @return the http response with the exception presentation
   */
  @Override
  public Response toResponse(E exception)
  {
    if (logger.isDebugEnabled())
    {
      StringBuilder msg = new StringBuilder();

      msg.append("map ").append(type.getSimpleName()).append("to status code ");
      msg.append(status.getStatusCode());
      logger.debug(msg.toString());
    }

    return Response.status(status)
      .entity(exception.getMessage())
      .type(MediaType.TEXT_PLAIN_TYPE)
      .build();
  }
}
