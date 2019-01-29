package sonia.scm.web.cgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultCGIExecutor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultCGIExecutorTest {

  @Mock
  private HttpServletRequest request;

  @Test
  public void testCreateCGIContentLength() {
    when(request.getHeader("Content-Length")).thenReturn("42");
    assertEquals("42", DefaultCGIExecutor.createCGIContentLength(request, false));
    assertEquals("42", DefaultCGIExecutor.createCGIContentLength(request, true));
  }

  @Test
  public void testCreateCGIContentLengthWithZeroLength() {
    when(request.getHeader("Content-Length")).thenReturn("0");
    assertEquals("", DefaultCGIExecutor.createCGIContentLength(request, false));
    assertEquals("-1", DefaultCGIExecutor.createCGIContentLength(request, true));
  }

  @Test
  public void testCreateCGIContentLengthWithoutContentLengthHeader() {
    assertEquals("", DefaultCGIExecutor.createCGIContentLength(request, false));
    assertEquals("-1", DefaultCGIExecutor.createCGIContentLength(request, true));
  }

  @Test
  public void testCreateCGIContentLengthWithLengthThatExeedsInteger() {
    when(request.getHeader("Content-Length")).thenReturn("6314297259");
    assertEquals("6314297259", DefaultCGIExecutor.createCGIContentLength(request, false));
  }

  @Test
  public void testCreateCGIContentLengthWithNonNumberHeader() {
    when(request.getHeader("Content-Length")).thenReturn("abc");
    assertEquals("", DefaultCGIExecutor.createCGIContentLength(request, false));
  }

}
