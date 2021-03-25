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

package sonia.scm.api.v2;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import sonia.scm.TransactionId;
import sonia.scm.api.v2.resources.ErrorDto;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
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
