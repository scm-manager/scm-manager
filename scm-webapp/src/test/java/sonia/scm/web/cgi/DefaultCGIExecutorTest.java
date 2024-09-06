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
