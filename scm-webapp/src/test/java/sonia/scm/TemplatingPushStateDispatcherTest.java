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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplatingPushStateDispatcherTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private TemplateEngine templateEngine;

  @Mock
  private Template template;

  @Mock
  private SCMContextProvider context;

  private TemplatingPushStateDispatcher dispatcher;

  @Before
  public void setUpMocks() {
    dispatcher = new TemplatingPushStateDispatcher(templateEngine, context);
  }

  @Test
  public void testDispatch() throws IOException {
    when(context.getStage()).thenReturn(Stage.DEVELOPMENT);

    TemplatingPushStateDispatcher.IndexHtmlModel model = dispatch();
    assertEquals("/scm", model.getContextPath());
    assertNull(model.getLiveReloadURL());
    assertEquals("DEVELOPMENT", model.getScmStage());
  }

  @Test
  public void testDispatchWithLiveReloadURL() throws IOException {
    System.setProperty("livereload.url", "/livereload.js");
    try {
      TemplatingPushStateDispatcher.IndexHtmlModel model = dispatch();
      assertEquals("/livereload.js", model.getLiveReloadURL());
    } finally {
      System.clearProperty("livereload.url");
    }
  }

  private TemplatingPushStateDispatcher.IndexHtmlModel dispatch() throws IOException {
    when(request.getContextPath()).thenReturn("/scm");
    when(templateEngine.getTemplate(TemplatingPushStateDispatcher.TEMPLATE)).thenReturn(template);

    when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

    dispatcher.dispatch(request, response, "/someurl");

    verify(response).setContentType("text/html");
    verify(response).setCharacterEncoding("UTF-8");

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

    verify(template).execute(any(Writer.class), captor.capture());

    return (TemplatingPushStateDispatcher.IndexHtmlModel) captor.getValue();
  }

}
