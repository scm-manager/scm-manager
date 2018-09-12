package sonia.scm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
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

  private TemplatingPushStateDispatcher dispatcher;

  @Before
  public void setUpMocks() {
    dispatcher = new TemplatingPushStateDispatcher(templateEngine);
  }

  @Test
  public void testDispatch() throws IOException {
    when(request.getContextPath()).thenReturn("/scm");
    when(templateEngine.getTemplate(TemplatingPushStateDispatcher.TEMPLATE)).thenReturn(template);

    when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

    dispatcher.dispatch(request, response, "/someurl");

    verify(response).setContentType("text/html");
    verify(response).setCharacterEncoding("UTF-8");

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

    verify(template).execute(any(Writer.class), captor.capture());

    TemplatingPushStateDispatcher.IndexHtmlModel model = (TemplatingPushStateDispatcher.IndexHtmlModel) captor.getValue();
    assertEquals("/scm", model.getContextPath());
  }

}
