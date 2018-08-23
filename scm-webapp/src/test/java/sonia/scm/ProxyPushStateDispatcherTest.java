package sonia.scm;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProxyPushStateDispatcherTest {

  private ProxyPushStateDispatcher dispatcher;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpURLConnection connection;

  @Before
  public void setUp() {
    dispatcher = new ProxyPushStateDispatcher("http://hitchhiker.com", url -> connection);
  }

  @Test
  public void testWithGetRequest() throws IOException {
    // configure request mock
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeaderNames()).thenReturn(toEnum("Content-Type"));
    when(request.getHeaders("Content-Type")).thenReturn(toEnum("application/json"));

    // configure proxy url connection mock
    when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("hitchhicker".getBytes(Charsets.UTF_8)));
    Map<String, List<String>> headerFields = new HashMap<>();
    headerFields.put("Content-Type", Lists.newArrayList("application/yaml"));
    when(connection.getHeaderFields()).thenReturn(headerFields);
    when(connection.getResponseCode()).thenReturn(200);

    // configure response mock
    DevServletOutputStream output = new DevServletOutputStream();
    when(response.getOutputStream()).thenReturn(output);

    dispatcher.dispatch(request, response, "/people/trillian");

    // verify connection
    verify(connection).setRequestMethod("GET");
    verify(connection).setRequestProperty("Content-Type", "application/json");

    // verify response
    verify(response).setStatus(200);
    verify(response).addHeader("Content-Type", "application/yaml");
    assertEquals("hitchhicker", output.stream.toString());
  }

  @Test
  public void testWithPOSTRequest() throws IOException {
    // configure request mock
    when(request.getMethod()).thenReturn("POST");
    when(request.getHeaderNames()).thenReturn(toEnum());
    when(request.getInputStream()).thenReturn(new DevServletInputStream("hitchhiker"));
    when(request.getContentLength()).thenReturn(1);

    // configure proxy url connection mock
    when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    Map<String, List<String>> headerFields = new HashMap<>();
    when(connection.getHeaderFields()).thenReturn(headerFields);
    when(connection.getResponseCode()).thenReturn(204);

    // configure response mock
    when(response.getOutputStream()).thenReturn(new DevServletOutputStream());

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    when(connection.getOutputStream()).thenReturn(output);

    dispatcher.dispatch(request, response, "/people/trillian");

    // verify connection
    verify(connection).setRequestMethod("POST");
    assertEquals("hitchhiker", output.toString());

    // verify response
    verify(response).setStatus(204);
  }

  private Enumeration<String> toEnum(String... values) {
    Set<String> set = ImmutableSet.copyOf(values);
    return toEnum(set);
  }

  private <T> Enumeration<T> toEnum(Collection<T> collection) {
    return new Vector<>(collection).elements();
  }

  private class DevServletInputStream extends ServletInputStream {

    private InputStream inputStream;

    private DevServletInputStream(String content) {
      inputStream = new ByteArrayInputStream(content.getBytes(Charsets.UTF_8));
    }

    @Override
    public int read() throws IOException {
      return inputStream.read();
    }
  }

  private class DevServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream stream = new ByteArrayOutputStream();

    @Override
    public void write(int b) {
      stream.write(b);
    }
  }

}
