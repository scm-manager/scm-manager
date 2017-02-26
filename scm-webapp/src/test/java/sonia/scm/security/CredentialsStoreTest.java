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

import com.google.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link CredentialsStore}.
 * 
 * @author Sebastian Sdorra
 * @since 1.52
 */
@RunWith(MockitoJUnitRunner.class)
public class CredentialsStoreTest {
  
  @Mock
  private HttpSession session;
  
  private CredentialsStore store;

  /**
   * Set up object under test.
   */
  @Before
  public void setUpObjectUnderTest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession(true)).thenReturn(session);
    Provider<HttpServletRequest> provider = mock(Provider.class);
    when(provider.get()).thenReturn(request);
    
    store = new TestableCredentialsStore(provider);
  }
  
  /**
   * Tests {@link CredentialsStore#store(org.apache.shiro.authc.UsernamePasswordToken)}.
   */
  @Test
  public void testStore() {
    store.store(new UsernamePasswordToken("trillian", "trillian123"));
    verify(session).setAttribute(CredentialsStore.SCM_CREDENTIALS, "x_trillian:trillian123");
  }

  private static class TestableCredentialsStore extends CredentialsStore {
  
    public TestableCredentialsStore(Provider<HttpServletRequest> requestProvider) {
      super(requestProvider);
    }

    @Override
    protected String encrypt(String credentials) {
      return "x_".concat(credentials);
    }
  
  }
  
}