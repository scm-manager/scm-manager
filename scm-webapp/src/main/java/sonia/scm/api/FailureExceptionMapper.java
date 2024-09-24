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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.jboss.resteasy.spi.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import java.util.Set;

@jakarta.ws.rs.ext.Provider
public class FailureExceptionMapper implements ExceptionMapper<Failure> {

  private static final Logger LOG = LoggerFactory.getLogger(FailureExceptionMapper.class);

  private static final Set<String> METHODS_WITHOUT_BODY = ImmutableSet.of("HEAD", "OPTIONS");

  @VisibleForTesting
  static final String ERROR_CODE = "2BSZikGOB1";

  private final Provider<HttpServletRequest> requestProvider;

  @Inject
  public FailureExceptionMapper(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  @Override
  public Response toResponse(Failure exception) {
    if (exception.isLoggable()) {
      LOG.warn("handle uncatched failure", exception);
    }
    Response.ResponseBuilder builder = builder(exception);
    if (shouldAppendErrorDto()) {
      ErrorDto errorDto = ErrorDtos.from(ERROR_CODE, exception);
      builder.entity(errorDto).type(VndMediaType.ERROR_TYPE);
    }
    return builder.build();
  }

  private Response.ResponseBuilder builder(Failure exception) {
    Response response = exception.getResponse();
    if (response != null) {
      return Response.fromResponse(response);
    }
    return Response.status(exception.getErrorCode());
  }

  private boolean shouldAppendErrorDto() {
    String method = requestProvider.get().getMethod();
    return !METHODS_WITHOUT_BODY.contains(method);
  }
}
