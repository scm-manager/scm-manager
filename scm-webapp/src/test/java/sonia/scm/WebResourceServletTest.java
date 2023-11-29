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
import com.google.common.io.Files;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.UberWebResourceLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class WebResourceServletTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private PluginLoader pluginLoader;

  @Mock
  private UberWebResourceLoader webResourceLoader;

  @Mock
  private PushStateDispatcher pushStateDispatcher;

  private WebResourceServlet servlet;

  @Before
  public void setUpMocks() {
    when(pluginLoader.getUberWebResourceLoader()).thenReturn(webResourceLoader);
    when(request.getContextPath()).thenReturn("/scm");
    servlet = new WebResourceServlet(pluginLoader, pushStateDispatcher);
  }

  @Test
  public void testPattern() {
    assertTrue("/some/resource".matches(WebResourceServlet.PATTERN));
    assertTrue("/favicon.ico".matches(WebResourceServlet.PATTERN));
    assertTrue("/other.html".matches(WebResourceServlet.PATTERN));
    assertFalse("/api/v2/repositories".matches(WebResourceServlet.PATTERN));

    // exclude old style ui template servlets
    assertTrue("/".matches(WebResourceServlet.PATTERN));
    assertTrue("/index.html".matches(WebResourceServlet.PATTERN));
    assertTrue("/error.html".matches(WebResourceServlet.PATTERN));
    assertTrue("/plugins/resources/js/sonia/scm/hg.config-wizard.js".matches(WebResourceServlet.PATTERN));
  }

  @Test
  public void testDoGetWithNonExistingResource() throws IOException {
    when(request.getRequestURI()).thenReturn("/scm/awesome.jpg");
    servlet.doGet(request, response);
    verify(pushStateDispatcher).dispatch(request, response, "/awesome.jpg");
  }


  @Test
  public void testDoGet() throws IOException {
    when(request.getRequestURI()).thenReturn("/scm/README.txt");
    TestingOutputServletOutputStream output = new TestingOutputServletOutputStream();
    when(response.getOutputStream()).thenReturn(output);

    File file = temporaryFolder.newFile();
    Files.write("hello".getBytes(Charsets.UTF_8), file);

    when(webResourceLoader.getResource("/README.txt")).thenReturn(file.toURI().toURL());
    servlet.doGet(request, response);

    assertEquals("hello", output.buffer.toString());
  }

  @Test
  public void testDoGetWithError() throws IOException {
    when(request.getRequestURI()).thenReturn("/scm/README.txt");
    ServletOutputStream output = mock(ServletOutputStream.class);
    when(response.getOutputStream()).thenReturn(output);

    File file = temporaryFolder.newFile();
    assertTrue(file.delete());

    when(webResourceLoader.getResource("/README.txt")).thenReturn(file.toURI().toURL());
    servlet.doGet(request, response);

    verify(output, never()).write(anyInt());
    verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testDoGetWithDispatcherException() throws IOException {
    when(request.getRequestURI()).thenReturn("/scm/awesome.jpg");
    doThrow(IOException.class).when(pushStateDispatcher).dispatch(request, response, "/awesome.jpg");
    servlet.doGet(request, response);
    verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  private static class TestingOutputServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    @Override
    public void write(int b) {
      buffer.write(b);
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
