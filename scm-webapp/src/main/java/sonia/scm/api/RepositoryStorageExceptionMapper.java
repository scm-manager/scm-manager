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
