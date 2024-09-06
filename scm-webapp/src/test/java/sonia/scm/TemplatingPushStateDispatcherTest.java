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
