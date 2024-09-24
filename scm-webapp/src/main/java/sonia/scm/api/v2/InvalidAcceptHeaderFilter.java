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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import sonia.scm.TransactionId;
import sonia.scm.api.v2.resources.ErrorDto;

import java.util.List;

@Provider
@Priority(ResponseFilterPriorities.INVALID_ACCEPT_HEADER)
public class InvalidAcceptHeaderFilter implements ContainerResponseFilter {

  @VisibleForTesting
  static final String CODE_PARTIAL_WILDCARD = "ChSSf2AFs1";

  private static final String MESSAGE_PARTIAL_WILDCARD = "Partial wildcards for media types are not supported. " +
    "Please use a valid content type or a non partial wildcard such as application/*.";

  @VisibleForTesting
  static final String CODE_APPLICATION_JSON = "7bSSf4F381";
  private static final String MESSAGE_APPLICATION_JSON = "Media type application/json is not supported on this url. " +
    "Please use a valid vnd media type.";

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)  {
    if (isNoMatchForAcceptHeader(responseContext)) {
      List<MediaType> acceptableMediaTypes = requestContext.getAcceptableMediaTypes();
      if (containsPartialWildcard(acceptableMediaTypes)) {
        responseContext.setEntity(createPartialWildcardError());
      } else if (containsApplicationJson(acceptableMediaTypes)) {
        responseContext.setEntity(createApplicationJsonError());
      }
    }
  }

  private boolean containsApplicationJson(List<MediaType> acceptableMediaTypes) {
    return acceptableMediaTypes.stream().anyMatch(this::isApplicationJson);
  }

  private ErrorDto createPartialWildcardError() {
    ErrorDto errorDto = new ErrorDto();
    errorDto.setErrorCode(CODE_PARTIAL_WILDCARD);
    errorDto.setMessage(MESSAGE_PARTIAL_WILDCARD);
    TransactionId.get().ifPresent(errorDto::setTransactionId);
    return errorDto;
  }

  private ErrorDto createApplicationJsonError() {
    ErrorDto errorDto = new ErrorDto();
    errorDto.setErrorCode(CODE_APPLICATION_JSON);
    errorDto.setMessage(MESSAGE_APPLICATION_JSON);
    TransactionId.get().ifPresent(errorDto::setTransactionId);
    return errorDto;
  }

  private boolean containsPartialWildcard(List<MediaType> acceptableMediaTypes) {
    return acceptableMediaTypes.stream().anyMatch(this::isPartialWildcard);
  }

  private boolean isPartialWildcard(MediaType mediaType) {
    if (mediaType.getSubtype() != null) {
      return mediaType.getSubtype().contains("*+json");
    }
    return true;
  }

  private boolean isApplicationJson(MediaType mediaType) {
    return "application".equals(mediaType.getType()) && "json".equals(mediaType.getSubtype());
  }

  private boolean isNoMatchForAcceptHeader(ContainerResponseContext responseContext) {
    if (responseContext.getStatus() == HttpServletResponse.SC_NOT_ACCEPTABLE) {
      Object entity = responseContext.getEntity();
      if (entity instanceof ErrorDto) {
        String message = ((ErrorDto) entity).getMessage();
        return Strings.nullToEmpty(message).startsWith("RESTEASY003635");
      }
    }
    return false;
  }

}
