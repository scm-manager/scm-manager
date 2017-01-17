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

package sonia.scm.security;

import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;

/**
 * Unit tests for {@link XsrfAccessTokenEnricher}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class XsrfAccessTokenEnricherTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private AccessTokenBuilder builder;
  
  private ScmConfiguration configuration;
  
  private XsrfAccessTokenEnricher enricher;
  
  /**
   * Prepare object under test.
   */
  @Before
  public void prepareObjectUnderTest() {
    configuration = new ScmConfiguration();
    enricher = new XsrfAccessTokenEnricher(configuration, () -> request) {
      @Override
      String createToken() {
        return "42";
      }
    };
  }
  
  /**
   * Tests {@link XsrfAccessTokenEnricher#enrich(java.util.Map)}.
   */
  @Test
  public void testEnrich() {
    // prepare
    configuration.setEnabledXsrfProtection(true);
    when(request.getHeader(HttpUtil.HEADER_SCM_CLIENT)).thenReturn(HttpUtil.SCM_CLIENT_WUI);
    
    // execute
    enricher.enrich(builder);
    
    // assert
    verify(builder).custom(Xsrf.TOKEN_KEY, "42");
  }
  
  /**
   * Tests {@link XsrfAccessTokenEnricher#enrich(java.util.Map)} with disabled xsrf protection.
   */
  @Test
  public void testEnrichWithDisabledXsrf() {
    // prepare
    configuration.setEnabledXsrfProtection(false);
    when(request.getHeader(HttpUtil.HEADER_SCM_CLIENT)).thenReturn(HttpUtil.SCM_CLIENT_WUI);
    
    // execute
    enricher.enrich(builder);
    
    // assert
    verify(builder, never()).custom(Xsrf.TOKEN_KEY, "42");
  }
  
 /**
   * Tests {@link XsrfAccessTokenEnricher#enrich(java.util.Map)} with disabled xsrf protection.
   */
  @Test
  public void testEnrichWithNonWuiClient() {
    // prepare
    configuration.setEnabledXsrfProtection(true);
    
    // execute
    enricher.enrich(builder);
    
    // assert
    verify(builder, never()).custom(Xsrf.TOKEN_KEY, "42");
  }

}