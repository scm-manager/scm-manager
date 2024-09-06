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
