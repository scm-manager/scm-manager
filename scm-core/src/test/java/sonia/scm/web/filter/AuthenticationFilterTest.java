/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
import org.mockito.runners.MockitoJUnitRunner;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.web.WebTokenGenerator;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
  public void testDoFilterWithAnonymousAccess()
    throws IOException, ServletException
  {
    configuration.setAnonymousAccessEnabled(true);

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
