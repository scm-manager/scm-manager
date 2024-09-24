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
