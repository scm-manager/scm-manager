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

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.UsernamePasswordToken;
import sonia.scm.web.security.AuthenticationManager;
import sonia.scm.web.security.AuthenticationResult;

/**
 * Facade for the authentication process. The main reason for this facade is to reduce the number of constructor 
 * parameters on the realm. This should improve testability.
 *
 * @author Sebastian Sdorra
 * @since 1.52
 */
public class AuthenticatorFacade {
  
  private final AuthenticationManager authenticator;
  private final Provider<HttpServletRequest> requestProvider;
  private final Provider<HttpServletResponse> responseProvider;

  @Inject
  public AuthenticatorFacade(AuthenticationManager authenticator, Provider<HttpServletRequest> requestProvider,
    Provider<HttpServletResponse> responseProvider) {
    this.authenticator = authenticator;
    this.requestProvider = requestProvider;
    this.responseProvider = responseProvider;
  }
  
  /**
   * Delegates the authentication request to the injected implementation of the {@link AuthenticationManager}.
   * 
   * @param token username password token
   * 
   * @return authentication result
   */
  public AuthenticationResult authenticate(UsernamePasswordToken token) {
    return authenticator.authenticate(
      requestProvider.get(), 
      responseProvider.get(),
      token.getUsername(), 
      new String(token.getPassword())
    );
  }
  
}
