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
    
package sonia.scm.filter;

import com.google.inject.util.Providers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GZipResponseFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private WriterInterceptorContext context;

  @Mock
  private MultivaluedMap<String,Object> headers;

  private GZipResponseFilter filter;

  @BeforeEach
  void setupObjectUnderTest() {
    filter = new GZipResponseFilter(Providers.of(request));
  }

  @Test
  void shouldSkipGZipCompression() throws IOException {
    when(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).thenReturn("deflate, br");

    filter.aroundWriteTo(context);

    verifySkipped();
  }

  @Test
  void shouldSkipGZipCompressionWithoutAcceptEncodingHeader() throws IOException {
    filter.aroundWriteTo(context);

    verifySkipped();
  }

  private void verifySkipped() throws IOException {
    verify(context, never()).getOutputStream();
    verify(context).proceed();
  }


  @Nested
  class AcceptGZipEncoding {

    @BeforeEach
    void setUpContext() {
      when(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).thenReturn("gzip, deflate, br");
      when(context.getHeaders()).thenReturn(headers);
      when(context.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    }

    @Test
    void shouldEncode() throws IOException {
      filter.aroundWriteTo(context);

      verify(headers).remove(HttpHeaders.CONTENT_LENGTH);
      verify(headers).add(HttpHeaders.CONTENT_ENCODING, "gzip");

      verify(context).setOutputStream(any(GZIPOutputStream.class));
      verify(context, times(2)).setOutputStream(any(OutputStream.class));
    }

  }


}
