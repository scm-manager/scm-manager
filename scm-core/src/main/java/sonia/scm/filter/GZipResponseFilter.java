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
