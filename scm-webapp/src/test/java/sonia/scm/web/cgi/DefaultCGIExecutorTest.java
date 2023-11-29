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
    
package sonia.scm.web.cgi;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
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
