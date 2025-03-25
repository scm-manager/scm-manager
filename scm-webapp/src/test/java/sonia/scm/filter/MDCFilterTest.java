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

package sonia.scm.filter;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;
import sonia.scm.AbstractTestBase;
import sonia.scm.SCMContext;
import sonia.scm.TransactionId;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MDCFilter}.
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class MDCFilterTest extends AbstractTestBase {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private final MDCFilter filter = new MDCFilter();

  /**
   * Tests {@link MDCFilter#doFilter(HttpServletRequest, HttpServletResponse, FilterChain)}.
   *
   * @throws IOException
   * @throws ServletException
   */
  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testDoFilter() throws IOException, ServletException
  {
    when(request.getRequestURI()).thenReturn("api/v1/repositories");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getRemoteHost()).thenReturn("localhost");
    when(request.getMethod()).thenReturn("GET");

    MDCCapturingFilterChain chain = new MDCCapturingFilterChain();
    filter.doFilter(request, response, chain);

    assertNotNull(chain.ctx);
    assertEquals("trillian", chain.ctx.get(MDCFilter.MDC_USERNAME));
    assertEquals("api/v1/repositories", chain.ctx.get(MDCFilter.MDC_REQUEST_URI));
    assertEquals("127.0.0.1", chain.ctx.get(MDCFilter.MDC_CLIENT_IP));
    assertEquals("localhost", chain.ctx.get(MDCFilter.MDC_CLIENT_HOST));
    assertEquals("GET", chain.ctx.get(MDCFilter.MDC_REQUEST_METHOD));
    assertNotNull(chain.ctx.get(TransactionId.KEY));
  }

  /**
   * Tests {@link MDCFilter#doFilter(HttpServletRequest, HttpServletResponse, FilterChain)} as anonymous user.
   *
   * @throws IOException
   * @throws ServletException
   */
  @Test
  @SubjectAware
  public void testDoFilterAsAnonymous() throws IOException, ServletException {
    MDCCapturingFilterChain chain = new MDCCapturingFilterChain();
    filter.doFilter(request, response, chain);

    assertNotNull(chain.ctx);
    assertEquals(SCMContext.USER_ANONYMOUS, chain.ctx.get(MDCFilter.MDC_USERNAME));
  }

  private static class MDCCapturingFilterChain implements FilterChain {

    private Map<String, String> ctx;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
      this.ctx = MDC.getCopyOfContextMap();
    }

  }

}
