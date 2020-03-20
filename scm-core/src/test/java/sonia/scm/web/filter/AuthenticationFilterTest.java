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
    
package sonia.scm.web.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.web.WebTokenGenerator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/shiro.ini")
public class AuthenticationFilterTest
{

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws ServletException
   */
  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void testDoFilterAuthenticated() throws IOException, ServletException
  {
    AuthenticationFilter filter = createAuthenticationFilter();

    filter.doFilter(request, response, chain);
    verify(chain).doFilter(any(HttpServletRequest.class),
      any(HttpServletResponse.class));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testDoFilterUnauthorized() throws IOException, ServletException
  {
    AuthenticationFilter filter = createAuthenticationFilter();

    filter.doFilter(request, response, chain);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
      "Authorization Required");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testDoFilterWithAuthenticationFailed()
    throws IOException, ServletException
  {
    AuthenticationFilter filter =
      createAuthenticationFilter(new DemoWebTokenGenerator("trillian", "sec"));

    filter.doFilter(request, response, chain);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
      "Authorization Required");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testDoFilterWithAuthenticationSuccess()
    throws IOException, ServletException
  {
    AuthenticationFilter filter =
      createAuthenticationFilter(new DemoWebTokenGenerator("trillian",
        "secret"));

    filter.doFilter(request, response, chain);
    verify(chain).doFilter(any(HttpServletRequest.class),
      any(HttpServletResponse.class));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
    configuration = new ScmConfiguration();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param generators
   *
   * @return
   */
  private AuthenticationFilter createAuthenticationFilter(
    WebTokenGenerator... generators)
  {
    return new AuthenticationFilter(configuration,
      ImmutableSet.copyOf(generators));
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 15/02/21
   * @author         Enter your name here...
   */
  private static class DemoWebTokenGenerator implements WebTokenGenerator
  {

    /**
     * Constructs ...
     *
     *
     * @param username
     * @param password
     */
    public DemoWebTokenGenerator(String username, String password)
    {
      this.username = username;
      this.password = password;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param request
     *
     * @return
     */
    @Override
    public AuthenticationToken createToken(HttpServletRequest request)
    {
      return new UsernamePasswordToken(username, password);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final String password;

    /** Field description */
    private final String username;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public ShiroRule shiro = new ShiroRule();

  /** Field description */
  @Mock
  private FilterChain chain;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  @Mock
  private HttpServletRequest request;

  /** Field description */
  @Mock
  private HttpServletResponse response;
}
