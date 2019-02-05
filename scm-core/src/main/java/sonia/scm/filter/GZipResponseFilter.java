package sonia.scm.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

@javax.ws.rs.ext.Provider
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
