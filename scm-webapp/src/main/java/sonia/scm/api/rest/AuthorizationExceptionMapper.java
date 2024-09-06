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
import jakarta.ws.rs.ext.Provider;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.Authentications;

/**
 *
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
