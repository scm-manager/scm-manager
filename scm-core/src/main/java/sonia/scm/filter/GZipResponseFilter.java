package sonia.scm.filter;

import lombok.extern.slf4j.Slf4j;
import sonia.scm.util.WebUtil;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

@Provider
@Slf4j
public class GZipResponseFilter implements ContainerResponseFilter {
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    if (WebUtil.isGzipSupported(requestContext::getHeaderString)) {
      log.trace("compress output with gzip");
      GZIPOutputStream wrappedResponse = new GZIPOutputStream(responseContext.getEntityStream());
      responseContext.setEntityStream(wrappedResponse);
    }
  }
}
