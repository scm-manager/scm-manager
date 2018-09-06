package sonia.scm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ForwardingPushStateDispatcherTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private RequestDispatcher requestDispatcher;

  @Mock
  private HttpServletResponse response;

  private ForwardingPushStateDispatcher dispatcher = new ForwardingPushStateDispatcher();

  @Test
  public void testDispatch() throws ServletException, IOException {
    when(request.getContextPath()).thenReturn("");
    when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

    dispatcher.dispatch(request, response, "/something");

    verify(requestDispatcher).forward(request, response);
  }

  @Test
  public void testDispatchWithContextPath() throws ServletException, IOException {
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestDispatcher("/scm/index.html")).thenReturn(requestDispatcher);

    dispatcher.dispatch(request, response, "/something");

    verify(requestDispatcher).forward(request, response);
  }

  @Test(expected = IOException.class)
  public void testWrapServletException() throws ServletException, IOException {
    when(request.getContextPath()).thenReturn("");
    when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);
    doThrow(ServletException.class).when(requestDispatcher).forward(request, response);

    dispatcher.dispatch(request, response, "/something");
  }

}
