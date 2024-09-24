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

package sonia.scm.api.v2;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import sonia.scm.api.v2.resources.ResteasyViolationExceptionToErrorDtoMapper;
import sonia.scm.web.VndMediaType;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  private final ResteasyViolationExceptionToErrorDtoMapper mapper;

  @Inject
  public ValidationExceptionMapper(ResteasyViolationExceptionToErrorDtoMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    return Response
      .status(Response.Status.BAD_REQUEST)
      .type(VndMediaType.ERROR_TYPE)
      .entity(mapper.map(exception))
      .build();
  }
}
