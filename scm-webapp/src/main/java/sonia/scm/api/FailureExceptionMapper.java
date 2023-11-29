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
