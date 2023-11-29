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
    
package sonia.scm.lifecycle.view;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SingleViewServletTest {

  @Mock
  private TemplateEngineFactory templateEngineFactory;

  @Mock
  private TemplateEngine templateEngine;

  @Mock
  private Template template;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private PrintWriter writer;

  @Mock
  private ViewController controller;

  @Test
  void shouldRenderTheTemplateOnGet() throws IOException {
    prepareTemplate("/template");
    doReturn(new View(200, "hello")).when(controller).createView(request);

    new SingleViewServlet(templateEngineFactory, controller).doGet(request, response);

    verifyResponse(200, "hello");
  }

  private void verifyResponse(int sc, Object model) throws IOException {
    verify(response).setStatus(sc);
    verify(response).setContentType("text/html");
    verify(response).setCharacterEncoding("UTF-8");

    verify(template).execute(writer, model);
  }

  @Test
  void shouldRenderTheTemplateOnPost() throws IOException {
    prepareTemplate("/template");

    doReturn(new View(201, "hello")).when(controller).createView(request);

    new SingleViewServlet(templateEngineFactory, controller).doPost(request, response);

    verifyResponse(201, "hello");
  }

  @Test
  void shouldThrowIllegalStateExceptionOnIOException() throws IOException {
    doReturn("/template").when(controller).getTemplate();
    doReturn(templateEngine).when(templateEngineFactory).getEngineByExtension("/template");
    doThrow(IOException.class).when(templateEngine).getTemplate("/template");

    assertThrows(IllegalStateException.class, () -> new SingleViewServlet(templateEngineFactory, controller));
  }

  private void prepareTemplate(String templatePath) throws IOException {
    doReturn(templateEngine).when(templateEngineFactory).getEngineByExtension(templatePath);
    doReturn(template).when(templateEngine).getTemplate(templatePath);
    doReturn(templatePath).when(controller).getTemplate();

    doReturn(writer).when(response).getWriter();
  }

}
