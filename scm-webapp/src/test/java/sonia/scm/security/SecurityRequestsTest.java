/**
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
package sonia.scm.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by masuewer on 04.07.18.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityRequestsTest {

  @Mock
  private HttpServletRequest request;

  @Test
  public void testIsAuthenticationRequestWithContextPath() {
    when(request.getRequestURI()).thenReturn("/scm/api/auth/access_token");
    when(request.getContextPath()).thenReturn("/scm");

    assertTrue(SecurityRequests.isAuthenticationRequest(request));
  }

  @Test
  public void testIsAuthenticationRequest() throws Exception {
    assertTrue(SecurityRequests.isAuthenticationRequest("/api/auth/access_token"));
    assertTrue(SecurityRequests.isAuthenticationRequest("/api/v2/auth/access_token"));
    assertFalse(SecurityRequests.isAuthenticationRequest("/api/repositories"));
    assertFalse(SecurityRequests.isAuthenticationRequest("/api/v2/repositories"));
  }
}
