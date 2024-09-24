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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.web.VndMediaType;

import static java.util.Arrays.asList;

@Slf4j
@Provider
public class RepositoryStorageExceptionMapper implements ExceptionMapper<RepositoryLocationResolver.RepositoryStorageException> {
  @Override
  public Response toResponse(RepositoryLocationResolver.RepositoryStorageException exception) {
    log.error("exception in repository storage", exception);
    ErrorDto error = new ErrorDto();
    error.setTransactionId(MDC.get("transaction_id"));
    error.setMessage("could not store repository: " + exception.getMessage());
    error.setErrorCode("E4TrutUSv1");
    ErrorDto.ConstraintViolationDto violation = new ErrorDto.ConstraintViolationDto();
    violation.setPath("storage location");
    violation.setMessage(exception.getRootMessage());
    error.setViolations(asList(violation));
    return Response.status(Response.Status.BAD_REQUEST)
      .entity(error)
      .type(VndMediaType.ERROR_TYPE)
      .build();
  }
}
