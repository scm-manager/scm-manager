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

package sonia.scm;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
    byte[] data = "hitchhicker".getBytes(Charsets.UTF_8);
    when(connection.getErrorStream()).thenReturn(null);
    when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(data));

    Map<String, List<String>> headerFields = new HashMap<>();
    headerFields.put("Content-Type", Lists.newArrayList("application/yaml"));
    when(connection.getHeaderFields()).thenReturn(headerFields);
    when(connection.getResponseCode()).thenReturn(200);
    when(connection.getContentLength()).thenReturn(data.length);

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
  public void testGetNotFound() throws IOException {
    // configure request mock
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeaderNames()).thenReturn(toEnum("Content-Type"));
    when(request.getHeaders("Content-Type")).thenReturn(toEnum("application/json"));

    // configure proxy url connection mock
    byte[] data = "hitchhicker".getBytes(Charsets.UTF_8);
    when(connection.getErrorStream()).thenReturn(new ByteArrayInputStream(data));

    Map<String, List<String>> headerFields = new HashMap<>();
    headerFields.put("Content-Type", Lists.newArrayList("application/yaml"));
    when(connection.getHeaderFields()).thenReturn(headerFields);
    when(connection.getResponseCode()).thenReturn(404);
    when(connection.getContentLength()).thenReturn(data.length);

    // configure response mock
    DevServletOutputStream output = new DevServletOutputStream();
    when(response.getOutputStream()).thenReturn(output);

    dispatcher.dispatch(request, response, "/people/trillian");

    // verify connection
    verify(connection).setRequestMethod("GET");
    verify(connection).setRequestProperty("Content-Type", "application/json");

    // verify response
    verify(response).setStatus(404);
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
    Map<String, List<String>> headerFields = new HashMap<>();
    when(connection.getHeaderFields()).thenReturn(headerFields);
    when(connection.getResponseCode()).thenReturn(204);

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

    private ByteArrayInputStream inputStream;

    private DevServletInputStream(String content) {
      inputStream = new ByteArrayInputStream(content.getBytes(Charsets.UTF_8));
    }

    @Override
    public int read() throws IOException {
      return inputStream.read();
    }

    @Override
    public boolean isReady() {
      return inputStream.available() > 0;
    }

    @Override
    public boolean isFinished() {
      return inputStream.available() == 0;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
    }
  }

  private class DevServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream stream = new ByteArrayOutputStream();

    @Override
    public void write(int b) {
      stream.write(b);
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }
  }

}
