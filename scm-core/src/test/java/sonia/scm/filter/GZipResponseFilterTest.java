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
