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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.Authentications;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Provider
public class AuthorizationExceptionMapper
  implements ExceptionMapper<AuthorizationException>
{

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizationExceptionMapper.class);

  @Override
  public Response toResponse(AuthorizationException exception) {
    LOG.info("user is missing permission: {}", exception.getMessage());
    LOG.trace(getStatus().toString(), exception);
    return Response.status(getStatus())
      .entity(exception.getMessage())
      .type(MediaType.TEXT_PLAIN_TYPE)
      .build();
  }

  private Response.Status getStatus() {
    return Authentications.isAuthenticatedSubjectAnonymous() ? Response.Status.UNAUTHORIZED : Response.Status.FORBIDDEN;
  }
}
