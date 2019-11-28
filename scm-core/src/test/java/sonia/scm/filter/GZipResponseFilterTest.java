package sonia.scm.filter;

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import static org.mockito.Mockito.*;

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
