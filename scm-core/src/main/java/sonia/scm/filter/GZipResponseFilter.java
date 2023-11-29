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

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

@jakarta.ws.rs.ext.Provider
public class GZipResponseFilter implements WriterInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(GZipResponseFilter.class);

  private final Provider<HttpServletRequest> requestProvider;

  @Inject
  public GZipResponseFilter(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    if (isGZipSupported()) {
      LOG.trace("compress output with gzip");
      encodeWithGZip(context);
    } else {
      context.proceed();
    }
  }

  private void encodeWithGZip(WriterInterceptorContext context) throws IOException {
    context.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
    context.getHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");

    OutputStream outputStream = context.getOutputStream();
    GZIPOutputStream compressedOutputStream = new GZIPOutputStream(outputStream);
    context.setOutputStream(compressedOutputStream);
    try {
      context.proceed();
    } finally {
      compressedOutputStream.finish();
      context.setOutputStream(outputStream);
    }
  }

  private boolean isGZipSupported() {
    Object encoding = requestProvider.get().getHeader(HttpHeaders.ACCEPT_ENCODING);
    return encoding != null && encoding.toString().toLowerCase(Locale.ENGLISH).contains("gzip");
  }
}
