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
