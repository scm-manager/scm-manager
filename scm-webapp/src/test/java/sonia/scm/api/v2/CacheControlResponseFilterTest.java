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

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheControlResponseFilterTest {

  @Mock
  private ContainerRequestContext requestContext;

  @Mock
  private ContainerResponseContext responseContext;

  @Mock
  private MultivaluedMap<String, Object> headers;

  private final CacheControlResponseFilter filter = new CacheControlResponseFilter();

  @BeforeEach
  void setUpMocks() {
    when(responseContext.getHeaders()).thenReturn(headers);
  }

  @Test
  void shouldAddCacheControlHeader() {
    filter.filter(requestContext, responseContext);

    verify(headers).add("Cache-Control", "no-cache");
  }

  @Test
  void shouldNotSetHeaderIfLastModifiedIsNotNull() {
    when(responseContext.getLastModified()).thenReturn(new Date());

    filter.filter(requestContext, responseContext);

    verify(headers, never()).add("Cache-Control", "no-cache");
  }

  @Test
  void shouldNotSetHeaderIfEtagIsNotNull() {
    when(responseContext.getEntityTag()).thenReturn(new EntityTag("42"));

    filter.filter(requestContext, responseContext);

    verify(headers, never()).add("Cache-Control", "no-cache");
  }

  @Test
  void shouldNotOverrideExistingCacheControl() {
    when(headers.containsKey("Cache-Control")).thenReturn(true);

    filter.filter(requestContext, responseContext);

    verify(headers, never()).add("Cache-Control", "no-cache");
  }

}
