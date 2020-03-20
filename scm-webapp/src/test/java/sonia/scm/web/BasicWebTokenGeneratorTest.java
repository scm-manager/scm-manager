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
    
package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;

/**
 * TODO add test with {@link UserAgentParser}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicWebTokenGeneratorTest
{
  
  /**
   * Set up object under test. 
   * Use {@code null} as {@link UserAgentParser}.
   */
  @Before
  public void setUpObjectUnderTest() {
    generator = new BasicWebTokenGenerator(null);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateToken()
  {
    String trillian = Base64.encodeToString("trillian:secret".getBytes());

    when(request.getHeader("Authorization")).thenReturn(
      "Basic ".concat(trillian));

    AuthenticationToken token = generator.createToken(request);

    assertThat(token, instanceOf(UsernamePasswordToken.class));

    UsernamePasswordToken upt = (UsernamePasswordToken) token;

    assertEquals("trillian", token.getPrincipal());
    assertArrayEquals("secret".toCharArray(), upt.getPassword());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateTokenWithWrongAuthorizationHeader()
  {
    when(request.getHeader("Authorization")).thenReturn("NONBASIC ASD");
    assertNull(generator.createToken(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateTokenWithWrongBasicAuthorizationHeader()
  {
    when(request.getHeader("Authorization")).thenReturn("Basic ASD");
    assertNull(generator.createToken(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateTokenWithoutAuthorizationHeader()
  {
    assertNull(generator.createToken(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateTokenWithoutPassword()
  {
    String trillian = Base64.encodeToString("trillian:".getBytes());

    when(request.getHeader("Authorization")).thenReturn(
      "Basic ".concat(trillian));
    assertNull(generator.createToken(request));
  }

  //~--- fields ---------------------------------------------------------------
  
  private BasicWebTokenGenerator generator;
  
  /** Field description */
  @Mock
  private HttpServletRequest request;
}
