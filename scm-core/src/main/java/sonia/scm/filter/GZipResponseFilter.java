package sonia.scm.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

@Provider
public class GZipResponseFilter implements WriterInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(GZipResponseFilter.class);

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    if (isGZipSupported(context)) {
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

  private boolean isGZipSupported(WriterInterceptorContext context) {
    Object encoding = context.getHeaders().getFirst(HttpHeaders.ACCEPT_ENCODING);
    return encoding != null && encoding.toString().toLowerCase(Locale.ENGLISH).contains("gzip");
  }
}
